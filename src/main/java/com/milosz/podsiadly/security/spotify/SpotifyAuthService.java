package com.milosz.podsiadly.security.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
public class SpotifyAuthService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    public SpotifyAuthService(
            @Value("${spotify.client-id}") String clientId,
            @Value("${spotify.client-secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webClient = WebClient.create("https://accounts.spotify.com");
    }

    public String getAccessToken() {
        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        return webClient.post()
                .uri("/api/token")
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block()
                .accessToken();
    }

    public record TokenResponse(@JsonProperty("access_token") String accessToken) {}
}
