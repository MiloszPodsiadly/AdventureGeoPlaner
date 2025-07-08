package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;
    private Long population;
    private String region;
    private String timezone;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "center_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "center_lon"))
    })
    private LatLon center;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    private List<TripPlan> plans;
}
