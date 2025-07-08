package com.milosz.podsiadly.ai;

import com.milosz.podsiadly.model.*;
import com.milosz.podsiadly.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SimpleAIBasedEngine implements RecommendationEngine {

    private final PlaceRepository placeRepository;

    @Override
    public List<Place> recommendPlaces(User user, City city) {
        return placeRepository.findByTripPlan_City_Id(city.getId()).stream()
                .sorted(Comparator.comparingInt(p -> scorePlace(p, user)))
                .limit(10)
                .collect(Collectors.toList());
    }

    private int scorePlace(Place place, User user) {
        int score = 0;
        if (place.isFavorite()) score += 20;
        if (place.getType() == PlaceType.MONUMENT) score += 10;
        if (place.getDescription().toLowerCase().contains("popular")) score += 5;
        // TODO: dodać preferencje usera, historię kliknięć itd.
        return -score; // sortujemy rosnąco, więc ujemne
    }
}
