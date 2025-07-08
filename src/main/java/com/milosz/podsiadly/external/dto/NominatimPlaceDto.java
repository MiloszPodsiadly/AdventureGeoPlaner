package com.milosz.podsiadly.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NominatimPlaceDto(
        String lat,
        String lon,
        @JsonProperty("display_name") String displayName
) {
}
