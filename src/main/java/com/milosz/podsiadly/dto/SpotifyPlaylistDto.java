package com.milosz.podsiadly.dto;

public record SpotifyPlaylistDto(
        Long    id,
        String  spotifyId,
        String  name,
        String  description,
        Boolean isPublic,
        Boolean collaborative,
        String  snapshotId,
        Integer trackCount,
        String  ownerSpotifyId,
        String  ownerDisplayName,
        String  externalUrl,
        String  imageUrl
) {}
