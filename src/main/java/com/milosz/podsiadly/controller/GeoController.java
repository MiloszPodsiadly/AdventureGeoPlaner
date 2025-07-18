// src/main/java/com/milosz/podsiadly/controller/GeoController.java
package com.milosz.podsiadly.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.service.NominatimService;
import com.milosz.podsiadly.service.OsrmRoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final NominatimService nominatimService;
    private final OsrmRoutingService routingService;

    /**
     * Import the top N “tourist attractions” for your TripPlan into the DB.
     */
    @PostMapping("/trip-plans/{tripPlanId}/places/import")
    public ResponseEntity<List<Place>> importPlaces(
            @PathVariable Long tripPlanId,
            @RequestParam(defaultValue="5") int limit) {

        List<Place> places = nominatimService.importPopularPlaces(tripPlanId, limit);
        return ResponseEntity.ok(places);
    }

    /**
     * Calculate a road between two LatLon points.
     */
    @GetMapping("/route")
    public ResponseEntity<JsonNode> getRoute(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon) {

        JsonNode routeJson = routingService.getRoute(
                new com.milosz.podsiadly.model.LatLon(startLat, startLon),
                new com.milosz.podsiadly.model.LatLon(endLat,   endLon)
        );
        return ResponseEntity.ok(routeJson);
    }
}
