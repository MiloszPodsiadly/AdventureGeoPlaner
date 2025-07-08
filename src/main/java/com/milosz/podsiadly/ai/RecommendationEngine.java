package com.milosz.podsiadly.ai;

import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.model.Place;

import java.util.List;

public interface RecommendationEngine {
    List<Place> recommendPlaces(User user, City city);

}
