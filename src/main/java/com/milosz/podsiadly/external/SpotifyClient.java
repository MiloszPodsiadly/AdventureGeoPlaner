package com.milosz.podsiadly.external;

import com.milosz.podsiadly.external.dto.SpotifyPlaylistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class SpotifyClient {

    private final WebClient webClient = WebClient.create("https://api.spotify.com");

    public SpotifyPlaylistResponse fetchFeaturedPlaylists(String token) {
        return webClient.get()
                .uri("/v1/browse/featured-playlists")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(SpotifyPlaylistResponse.class)
                .block();
    }
}
