package com.milosz.podsiadly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.model.*;
import com.milosz.podsiadly.repository.PlaceRepository;
import com.milosz.podsiadly.repository.RouteRepository;
import com.milosz.podsiadly.repository.TripPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final TripPlanRepository tripPlanRepository;
    private final PlaceRepository placeRepository;
    private final LatLogService latLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Route generateRouteForTripPlan(Long tripPlanId) {
        TripPlan tripPlan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan not found"));

        List<Place> places = placeRepository.findByTripPlanIdOrderByOrderIndexAsc(tripPlanId);

        if (places.size() < 2) {
            throw new IllegalArgumentException("At least two places are required");
        }

        if (places.stream().anyMatch(p -> p.getLocation() == null)) {
            throw new IllegalArgumentException("All places must have a location");
        }

        LatLon start = places.get(0).getLocation();
        LatLon end = places.get(places.size() - 1).getLocation();
        List<LatLon> waypoints = places.stream().map(Place::getLocation).toList();

        double totalDistance = 0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            totalDistance += latLogService.calculateDistanceKm(waypoints.get(i), waypoints.get(i + 1));
        }

        String pathJson;
        try {
            pathJson = objectMapper.writeValueAsString(waypoints);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize route waypoints", e);
        }

        Route route = Route.builder()
                .start(start)
                .end(end)
                .distance(totalDistance)
                .duration(totalDistance / 5 * 60) // np. 5 km/h tempo -> minuty
                .pathJson(pathJson)
                .build();

        Route saved = routeRepository.save(route);
        tripPlan.setRoute(saved);
        tripPlanRepository.save(tripPlan);

        return saved;
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route not found"));
    }
}
