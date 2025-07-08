package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.PlaceDto;
import com.milosz.podsiadly.mapper.PlaceMapper;
import com.milosz.podsiadly.model.PlaceType;
import com.milosz.podsiadly.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    @GetMapping
    public ResponseEntity<List<PlaceDto>> getAllPlaces() {
        List<PlaceDto> places = placeMapper.mapToDtoList(placeService.getAllPlaces());
        return ResponseEntity.ok(places);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDto> getPlaceById(@PathVariable Long id) {
        return ResponseEntity.ok(placeMapper.mapToDto(placeService.getPlaceById(id)));
    }

    @GetMapping("/trip/{tripPlanId}")
    public ResponseEntity<List<PlaceDto>> getPlacesByTripPlan(@PathVariable Long tripPlanId) {
        List<PlaceDto> places = placeMapper.mapToDtoList(placeService.getPlacesByTripPlan(tripPlanId));
        return ResponseEntity.ok(places);
    }

    @PostMapping("/trip/{tripPlanId}")
    public ResponseEntity<PlaceDto> createPlace(@PathVariable Long tripPlanId, @RequestBody PlaceDto dto) {
        var place = placeService.createPlace(tripPlanId, placeMapper.mapToEntity(dto));
        return ResponseEntity.ok(placeMapper.mapToDto(place));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceDto> updatePlace(@PathVariable Long id, @RequestBody PlaceDto dto) {
        var place = placeService.updatePlace(id, placeMapper.mapToEntity(dto));
        return ResponseEntity.ok(placeMapper.mapToDto(place));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id, @RequestParam boolean favorite) {
        placeService.toggleFavorite(id, favorite);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/order")
    public ResponseEntity<Void> updateOrder(@PathVariable Long id, @RequestParam int index) {
        placeService.updateOrderIndex(id, index);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<List<PlaceDto>> importPlaces(
            @RequestParam Long tripPlanId,
            @RequestParam PlaceType type,
            @RequestParam(required = false) String query
    ) {
        List<PlaceDto> places = placeMapper.mapToDtoList(
                placeService.importPlacesFromExternalApi(tripPlanId, type, query)
        );
        return ResponseEntity.ok(places);
    }

    @GetMapping("/recommendations/{cityId}")
    public ResponseEntity<Map<PlaceDto, Long>> getMostRecommended(@PathVariable Long cityId) {
        var result = placeService.getMostRecommendedPlacesInCity(cityId)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> placeMapper.mapToDto(entry.getKey()),
                        Map.Entry::getValue
                ));
        return ResponseEntity.ok(result);
    }
}
