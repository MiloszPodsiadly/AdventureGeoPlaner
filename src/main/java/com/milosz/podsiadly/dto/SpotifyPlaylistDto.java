package com.milosz.podsiadly.dto;

public record SpotifyPlaylistDto(
        Long id,
        String spotifyId,
        String name,
        String description,
        String imageUrl,
        String externalUrl,
        Long userId
) {}
