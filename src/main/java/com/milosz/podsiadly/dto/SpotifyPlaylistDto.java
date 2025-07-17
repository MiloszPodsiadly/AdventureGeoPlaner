package com.milosz.podsiadly.dto;

public record SpotifyPlaylistDto(
        Long id,
        String spotifyId,
        String name,
        String description,
        Boolean isPublic,
        Boolean collaborative,
        String ownerDisplayName,
        String ownerSpotifyId,
        String externalUrl,
        String imageUrl
) {}
