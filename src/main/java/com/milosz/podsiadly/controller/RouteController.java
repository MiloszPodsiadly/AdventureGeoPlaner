package com.milosz.podsiadly.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.dto.RouteDto;
import com.milosz.podsiadly.mapper.RouteMapper;
import com.milosz.podsiadly.model.Route;
import com.milosz.podsiadly.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/generate/{tripPlanId}")
    public ResponseEntity<RouteDto> generate(@PathVariable Long tripPlanId) {
        Route route = routeService.generateRouteForTripPlan(tripPlanId);
        return ResponseEntity.ok(RouteMapper.mapToDto(route));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                RouteMapper.mapToDto(routeService.getRouteById(id))
        );
    }

    // 🔁 GeoJSON export – do mapy na froncie
    @GetMapping("/{id}/geojson")
    public ResponseEntity<Map<String, Object>> exportGeoJson(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        Map<String, Object> geoJson = Map.of(
                "type", "Feature",
                "geometry", Map.of(
                        "type", "LineString",
                        "coordinates", RouteMapper.mapToCoordinatesList(route)
                ),
                "properties", Map.of(
                        "distance", route.getDistance(),
                        "duration", route.getDuration()
                )
        );
        return ResponseEntity.ok(geoJson);
    }
}
