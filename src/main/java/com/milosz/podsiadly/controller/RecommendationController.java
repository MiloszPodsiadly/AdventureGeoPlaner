package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.RecommendationDto;
import com.milosz.podsiadly.mapper.RecommendationMapper;
import com.milosz.podsiadly.model.Recommendation;
import com.milosz.podsiadly.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getAll() {
        return ResponseEntity.ok(
                recommendationService.getAll().stream()
                        .map(RecommendationMapper::mapToDto)
                        .toList()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendationDto>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                recommendationService.getByUserId(userId).stream()
                        .map(RecommendationMapper::mapToDto)
                        .toList()
        );
    }

    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<RecommendationDto>> getByPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(
                recommendationService.getByPlaceId(placeId).stream()
                        .map(RecommendationMapper::mapToDto)
                        .toList()
        );
    }

    @PostMapping("/{userId}/{placeId}")
    public ResponseEntity<RecommendationDto> create(
            @PathVariable Long userId,
            @PathVariable Long placeId,
            @RequestParam String reason,
            @RequestParam String source
    ) {
        Recommendation created = recommendationService.createRecommendation(userId, placeId, reason, source);
        return ResponseEntity.ok(RecommendationMapper.mapToDto(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rate(@PathVariable Long id, @RequestParam int rating) {
        recommendationService.rateRecommendation(id, rating);
        return ResponseEntity.ok().build();
    }

    // 🔁 AI-generated recommendations for dashboard
    @GetMapping("/ai/city/{cityId}/user/{userId}")
    public ResponseEntity<List<RecommendationDto>> recommendForCity(
            @PathVariable Long userId,
            @PathVariable Long cityId
    ) {
        List<Recommendation> ai = recommendationService.recommendForCity(userId, cityId);
        return ResponseEntity.ok(ai.stream().map(RecommendationMapper::mapToDto).toList());
    }
}
