package com.milosz.podsiadly.dto;

import java.time.Instant;
import java.time.LocalDate;

public record TripPlanDto(
        Long id,
        String title,
        String description,
        LocalDate date,
        Long cityId,
        Long userId,
        Long routeId,
        Long spotifyPlaylistId,
        Instant createdAt,
        Instant updatedAt
) {}

