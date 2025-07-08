package com.milosz.podsiadly.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record SpotifyPlaylistResponse(
        Playlists playlists
) {
    public record Playlists(
            List<Item> items
    ) {}

    public record Item(
            String id,
            String name,
            String description,
            List<Image> images,
            @JsonProperty("external_urls") Map<String, String> externalUrls
    ) {}

    public record Image(
            String url
    ) {}
}
