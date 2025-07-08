package com.milosz.podsiadly.dto;

public record RouteDto(
        Long id,
        double distance,
        double duration,
        LatLonDto start,
        LatLonDto end,
        String pathJson
) {}
