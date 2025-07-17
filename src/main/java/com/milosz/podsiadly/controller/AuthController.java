package com.milosz.podsiadly.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.UserRepository;
import com.milosz.podsiadly.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/spotify")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spotify.token-url}")
    private String spotifyTokenUrl;

    @Value("${spotify.api-url}")
    private String spotifyApiUrl;

    // In‑memory store for OAuth2 state
    private final Map<String,Boolean> validStates = new ConcurrentHashMap<>();

    /**
     * STEP 1: Redirect the user to Spotify's /authorize endpoint.
     */
    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String state = UUID.randomUUID().toString();
        validStates.put(state, true);

        String url = UriComponentsBuilder
                .fromUriString("https://accounts.spotify.com/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", "user-read-email playlist-read-private")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    /**
     * STEP 2: Spotify calls back here with ?code=...&state=...
     * We exchange the code for tokens, fetch the user profile,
     * upsert the User in our DB (offloaded to boundedElastic),
     * then issue our own JWT.
     */
    @GetMapping("/success")
    public Mono<ResponseEntity<String>> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        // Validate state
        if (!Boolean.TRUE.equals(validStates.remove(state))) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid state"));
        }

        // Exchange authorization code for tokens
        return webClientBuilder.build()
                .post()
                .uri(spotifyTokenUrl)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", code)
                        .with("redirect_uri", redirectUri))
                .exchangeToMono(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(
                                        new RuntimeException("Token exchange failed: " + err)
                                ));
                    }
                    return resp.bodyToMono(JsonNode.class);
                })
                // Fetch the Spotify user profile
                .flatMap(tokenJson -> {
                    String accessToken  = tokenJson.get("access_token").asText();
                    String refreshToken = tokenJson.get("refresh_token").asText();
                    long   expiresIn    = tokenJson.get("expires_in").asLong();

                    return webClientBuilder.build()
                            .get()
                            .uri(spotifyApiUrl + "/me")
                            .headers(h -> h.setBearerAuth(accessToken))
                            .exchangeToMono(resp -> {
                                if (resp.statusCode().isError()) {
                                    return resp.bodyToMono(String.class)
                                            .flatMap(err -> Mono.error(
                                                    new RuntimeException("Profile fetch failed: " + err)
                                            ));
                                }
                                return resp.bodyToMono(JsonNode.class);
                            })
                            // Offload DB I/O and JWT generation
                            .flatMap(meJson -> {
                                String spotifyId   = meJson.get("id").asText();
                                String email       = meJson.get("email").asText();
                                String displayName = meJson.get("display_name").asText();

                                return Mono.fromCallable(() -> {
                                            // Upsert user in blocking way
                                            return userRepository.findBySpotifyId(spotifyId)
                                                    .map(u -> {
                                                        u.setSpotifyAccessToken(accessToken);
                                                        u.setSpotifyRefreshToken(refreshToken);
                                                        u.setSpotifyTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
                                                        return userRepository.save(u);
                                                    })
                                                    .orElseGet(() -> userRepository.save(User.builder()
                                                            .spotifyId(spotifyId)
                                                            .email(email)
                                                            .displayName(displayName)
                                                            .provider("SPOTIFY")
                                                            .role("USER")
                                                            .spotifyAccessToken(accessToken)
                                                            .spotifyRefreshToken(refreshToken)
                                                            .spotifyTokenExpiresAt(Instant.now().plusSeconds(expiresIn))
                                                            .build()
                                                    ));
                                        })
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .map(user -> {
                                            // Generate our JWT
                                            String jwt = jwtUtil.generateToken(
                                                    new org.springframework.security.core.userdetails.User(
                                                            user.getEmail(), "",
                                                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                                    )
                                            );
                                            return ResponseEntity.ok(jwt);
                                        });
                            });
                });
    }
}
