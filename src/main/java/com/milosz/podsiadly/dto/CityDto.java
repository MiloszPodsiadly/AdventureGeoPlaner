package com.milosz.podsiadly.dto;

public record CityDto(
        Long id,
        String name,
        String country,
        Long population,
        String region,
        String timezone,
        double lat,
        double lng
) {}

