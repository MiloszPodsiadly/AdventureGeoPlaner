package com.milosz.podsiadly.service;

import com.milosz.podsiadly.external.PlaceApiClient;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.PlaceType;
import com.milosz.podsiadly.model.TripPlan;
import com.milosz.podsiadly.repository.PlaceRepository;
import com.milosz.podsiadly.repository.TripPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import com.milosz.podsiadly.model.Recommendation;
import com.milosz.podsiadly.repository.RecommendationRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TripPlanRepository tripPlanRepository;
    private final PlaceApiClient placeApiClient;
    private final RecommendationRepository recommendationRepository;

    public Map<Place, Long> getMostRecommendedPlacesInCity(Long cityId) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getPlace().getTripPlan().getCity().getId().equals(cityId))
                .collect(Collectors.groupingBy(Recommendation::getPlace, Collectors.counting()));
    }

    /**
     * Zwraca wszystkie miejsca.
     */
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    /**
     * Zwraca jedno miejsce po ID.
     */
    public Place getPlaceById(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Place not found with id: " + id));
    }

    /**
     * Zwraca miejsca przypisane do danego planu.
     */
    public List<Place> getPlacesByTripPlan(Long tripPlanId) {
        TripPlan tripPlan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Trip plan not found with id: " + tripPlanId));
        return placeRepository.findByTripPlan(tripPlan);
    }

    /**
     * Tworzy nowe miejsce ręcznie (np. z UI).
     */
    @Transactional
    public Place createPlace(Long tripPlanId, Place place) {
        validate(place);
        TripPlan plan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Trip plan not found with id: " + tripPlanId));
        place.setTripPlan(plan);
        return placeRepository.save(place);
    }

    /**
     * Aktualizuje istniejące miejsce.
     */
    @Transactional
    public Place updatePlace(Long id, Place updated) {
        validate(updated);
        Place existing = getPlaceById(id);

        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setOrderIndex(updated.getOrderIndex());
        existing.setFavorite(updated.isFavorite());

        return placeRepository.save(existing);
    }

    /**
     * Oznacza miejsce jako ulubione / nieulubione.
     */
    @Transactional
    public void toggleFavorite(Long id, boolean isFavorite) {
        Place place = getPlaceById(id);
        place.setFavorite(isFavorite);
        placeRepository.save(place);
    }

    /**
     * Ustawia nową kolejność miejsca w trasie.
     */
    @Transactional
    public void updateOrderIndex(Long id, int newIndex) {
        Place place = getPlaceById(id);
        place.setOrderIndex(newIndex);
        placeRepository.save(place);
    }

    /**
     * Usuwa miejsce z trasy.
     */
    @Transactional
    public void deletePlace(Long id) {
        if (!placeRepository.existsById(id)) {
            throw new EntityNotFoundException("Place not found with id: " + id);
        }
        placeRepository.deleteById(id);
    }

    /**
     * Pobiera miejsca z API zewnętrznego (np. Foursquare) i zapisuje do planu.
     */
    @Transactional
    public List<Place> importPlacesFromExternalApi(Long tripPlanId, PlaceType type, String query) {
        TripPlan tripPlan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Trip plan not found with id: " + tripPlanId));

        List<Place> externalPlaces = placeApiClient.fetchPlaces(type, tripPlan.getCity(), query);
        for (Place place : externalPlaces) {
            place.setTripPlan(tripPlan);
        }

        return placeRepository.saveAll(externalPlaces);
    }

    /**
     * Walidacja pól obiektu Place.
     */
    private void validate(Place place) {
        if (place == null) throw new IllegalArgumentException("Place cannot be null");
        if (StringUtils.isBlank(place.getName())) throw new IllegalArgumentException("Place name is required");
        if (place.getType() == null) throw new IllegalArgumentException("Place type is required");
        if (place.getLocation() == null) throw new IllegalArgumentException("Place location is required");
    }
}
