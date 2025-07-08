package com.milosz.podsiadly.dto;

import java.time.LocalDateTime;

public record RecommendationDto(
        Long id,
        String reason,
        String source,
        Long userId,
        Long placeId,
        LocalDateTime createdAt
) {}
