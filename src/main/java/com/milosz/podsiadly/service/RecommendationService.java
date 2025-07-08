package com.milosz.podsiadly.service;

import com.milosz.podsiadly.ai.RecommendationEngine;
import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.Recommendation;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.CityRepository;
import com.milosz.podsiadly.repository.PlaceRepository;
import com.milosz.podsiadly.repository.RecommendationRepository;
import com.milosz.podsiadly.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final RecommendationEngine recommendationEngine;

    public List<Place> getAIBasedRecommendationsForUser(User user, City city) {
        return recommendationEngine.recommendPlaces(user, city);
    }
    @Transactional
    public void rateRecommendation(Long id, int rating) {
        Recommendation r = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
        if (rating < -1 || rating > 1) throw new IllegalArgumentException("Rating must be -1, 0 or 1");
        r.setRating(rating);
    }

    public List<Recommendation> getAll() {
        return recommendationRepository.findAll();
    }

    public List<Recommendation> getByUserId(Long userId) {
        return recommendationRepository.findByUserId(userId);
    }

    public List<Recommendation> getByPlaceId(Long placeId) {
        return recommendationRepository.findByPlaceId(placeId);
    }

    @Transactional
    public Recommendation createRecommendation(Long userId, Long placeId, String reason, String source) {
        if (StringUtils.isBlank(reason)) throw new IllegalArgumentException("Reason is required");
        if (StringUtils.isBlank(source)) throw new IllegalArgumentException("Source is required");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found"));

        Recommendation rec = Recommendation.builder()
                .user(user)
                .place(place)
                .reason(reason)
                .source(source)
                .createdAt(LocalDateTime.now())
                .build();

        return recommendationRepository.save(rec);
    }

    @Transactional
    public void deleteRecommendation(Long id) {
        if (!recommendationRepository.existsById(id)) {
            throw new EntityNotFoundException("Recommendation not found");
        }
        recommendationRepository.deleteById(id);
    }
    @Transactional
    public List<Recommendation> recommendForCity(Long userId, Long cityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + cityId));

        // 🧠 Wygeneruj listę miejsc z AI/ML engine
        List<Place> aiPlaces = recommendationEngine.recommendPlaces(user, city);

        // ✨ Utwórz nowe rekomendacje (ale nie zapisuj duplikatów!)
        List<Recommendation> recommendations = aiPlaces.stream()
                .filter(place -> recommendationRepository.findByUserId(userId).stream()
                        .noneMatch(r -> r.getPlace().getId().equals(place.getId())))
                .map(place -> Recommendation.builder()
                        .user(user)
                        .place(place)
                        .reason("AI Suggested")
                        .source("AI")
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        return recommendationRepository.saveAll(recommendations);
    }

}
