package com.milosz.podsiadly.dto;

public record PlaceDto(
        Long id,
        String name,
        String description,
        String type,
        LatLonDto location,          // ✅ zamiast lat/lon osobno
        Integer orderIndex,
        boolean isFavorite,
        Long tripPlanId
) {}

