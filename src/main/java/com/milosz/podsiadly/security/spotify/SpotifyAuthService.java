package com.milosz.podsiadly.security.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.milosz.podsiadly.config.SpotifyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class SpotifyAuthService {

    private final WebClient webClient = WebClient.create("https://accounts.spotify.com");
    private final SpotifyProperties spotifyProperties;

    public String getAccessToken() {
        String credentials = Base64.getEncoder()
                .encodeToString((spotifyProperties.getClientId() + ":" + spotifyProperties.getClientSecret()).getBytes());

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
