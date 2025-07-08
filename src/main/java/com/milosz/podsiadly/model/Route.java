package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double distance;
    private double duration;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "start_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "start_lon"))
    })
    private LatLon start;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "end_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "end_lon"))
    })
    private LatLon end;

    @Lob
    @Column(name = "path_json")
    private String pathJson;
}
