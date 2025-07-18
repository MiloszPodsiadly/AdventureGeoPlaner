package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.TripPlanDto;
import com.milosz.podsiadly.mapper.TripPlanMapper;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.Route;
import com.milosz.podsiadly.model.TripPlan;
import com.milosz.podsiadly.service.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-plans")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @GetMapping
    public ResponseEntity<List<TripPlanDto>> getAll() {
        var plans = tripPlanService.getAllPlans();
        return ResponseEntity.ok(plans.stream().map(TripPlanMapper::mapToDto).toList());
    }

    /**
     * POST /api/trip-plans/{planId}/places/import?limit=5
     */
    @PostMapping("/{planId}/places/import")
    public ResponseEntity<List<Place>> importPlaces(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Place> places = tripPlanService.importPopularPlaces(planId, limit);
        return ResponseEntity.ok(places);
    }

    /**
     * POST /api/trip-plans/{planId}/route
     */
    @PostMapping("/{planId}/route")
    public ResponseEntity<TripPlan> assignRoute(
            @PathVariable Long planId) {
        TripPlan updated = tripPlanService.calculateAndAssignRoute(planId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{planId}/playlist/{playlistId}")
    public ResponseEntity<TripPlanDto> assignPlaylist(
            @PathVariable Long planId,
            @PathVariable Long playlistId) {

        TripPlan updated = tripPlanService.assignPlaylistToPlan(planId, playlistId);
        return ResponseEntity.ok(TripPlanMapper.mapToDto(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripPlanDto> getById(@PathVariable Long id) {
        TripPlan plan = tripPlanService.getTripPlanById(id);
        return ResponseEntity.ok(TripPlanMapper.mapToDto(plan));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TripPlanDto>> getByUser(@PathVariable Long userId) {
        var plans = tripPlanService.getPlansByUserId(userId);
        return ResponseEntity.ok(plans.stream().map(TripPlanMapper::mapToDto).toList());
    }

    @PostMapping("/user/{userId}/city/{cityId}")
    public ResponseEntity<TripPlanDto> create(
            @PathVariable Long userId,
            @PathVariable Long cityId,
            @RequestParam(required = false) Long playlistId,  // nowy parametr
            @RequestBody TripPlan tripPlan
    ) {
        TripPlan created = tripPlanService.createTripPlan(userId, cityId, tripPlan, playlistId);
        return ResponseEntity.ok(TripPlanMapper.mapToDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripPlanDto> update(
            @PathVariable Long id,
            @RequestParam(required = false) Long playlistId,  // nowy parametr
            @RequestBody TripPlan tripPlan
    ) {
        TripPlan updated = tripPlanService.updateTripPlan(id, tripPlan, playlistId);
        return ResponseEntity.ok(TripPlanMapper.mapToDto(updated));
    }

    @PutMapping("/{id}/assign-route")
    public ResponseEntity<TripPlanDto> assignRoute(
            @PathVariable Long id,
            @RequestBody Route route
    ) {
        TripPlan plan = tripPlanService.assignRouteToPlan(id, route);
        return ResponseEntity.ok(TripPlanMapper.mapToDto(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripPlanService.deleteTripPlan(id);
        return ResponseEntity.noContent().build();
    }
}
