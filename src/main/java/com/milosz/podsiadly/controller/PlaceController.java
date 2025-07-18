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

    @GetMapping
    public ResponseEntity<List<PlaceDto>> getAllPlaces() {
        List<PlaceDto> dtos = PlaceMapper.toDtoList(placeService.getAllPlaces());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDto> getPlaceById(@PathVariable Long id) {
        var place = placeService.getPlaceById(id);
        return ResponseEntity.ok(PlaceMapper.toDto(place));
    }

    @GetMapping("/trip/{tripPlanId}")
    public ResponseEntity<List<PlaceDto>> getPlacesByTripPlan(@PathVariable Long tripPlanId) {
        List<PlaceDto> dtos = PlaceMapper.toDtoList(
                placeService.getPlacesByTripPlan(tripPlanId)
        );
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/trip/{tripPlanId}")
    public ResponseEntity<PlaceDto> createPlace(
            @PathVariable Long tripPlanId,
            @RequestBody PlaceDto dto
    ) {
        var entity = PlaceMapper.fromDto(dto);
        var created = placeService.createPlace(tripPlanId, entity);
        return ResponseEntity.ok(PlaceMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceDto> updatePlace(
            @PathVariable Long id,
            @RequestBody PlaceDto dto
    ) {
        var entity = PlaceMapper.fromDto(dto);
        var updated = placeService.updatePlace(id, entity);
        return ResponseEntity.ok(PlaceMapper.toDto(updated));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable Long id,
            @RequestParam boolean favorite
    ) {
        placeService.toggleFavorite(id, favorite);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/order")
    public ResponseEntity<Void> updateOrder(
            @PathVariable Long id,
            @RequestParam int index
    ) {
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
        List<PlaceDto> dtos = PlaceMapper.toDtoList(
                placeService.importPlacesFromExternalApi(tripPlanId, type, query)
        );
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/recommendations/{cityId}")
    public ResponseEntity<Map<PlaceDto, Long>> getMostRecommended(
            @PathVariable Long cityId
    ) {
        var map = placeService.getMostRecommendedPlacesInCity(cityId).entrySet().stream()
                .collect(Collectors.toMap(
                        e -> PlaceMapper.toDto(e.getKey()),
                        Map.Entry::getValue
                ));
        return ResponseEntity.ok(map);
    }
}
