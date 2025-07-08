package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "city_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category;
    private String source;
    private String externalUrl;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "location_lon"))
    })
    private LatLon location;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
