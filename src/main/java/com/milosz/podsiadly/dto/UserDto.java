package com.milosz.podsiadly.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String email,
        String displayName,
        String spotifyId,
        String provider,
        String role,
        Instant createdAt,
        Instant updatedAt
) {}
