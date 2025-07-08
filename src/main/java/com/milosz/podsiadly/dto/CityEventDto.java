package com.milosz.podsiadly.dto;


import java.time.LocalDateTime;

public record CityEventDto(
        Long id,
        String title,
        String description,
        String category,
        String source,
        String externalUrl,
        Long cityId,
        double lat,
        double lng,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}

