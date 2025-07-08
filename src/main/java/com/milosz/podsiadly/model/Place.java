package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PlaceType type;

    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "location_lon"))
    })
    private LatLon location;

    private Integer orderIndex;

    private boolean favorite;

    @ManyToOne
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;
}
