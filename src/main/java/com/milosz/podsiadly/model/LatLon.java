package com.milosz.podsiadly.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LatLon {

    @Min(value = -90, message = "Latitude cannot be less than -90")
    @Max(value = 90, message = "Latitude cannot be greater than 90")
    private double lat;

    @Min(value = -180, message = "Longitude cannot be less than -180")
    @Max(value = 180, message = "Longitude cannot be greater than 180")
    private double lon;
}
