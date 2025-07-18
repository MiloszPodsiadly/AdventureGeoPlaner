package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.LatLon;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.PlaceType;
import com.milosz.podsiadly.model.TripPlan;
import com.milosz.podsiadly.repository.PlaceRepository;
import com.milosz.podsiadly.repository.TripPlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.IntStream;

@Service

public class NominatimService {

    private final WebClient nominatimClient;
    private final TripPlanRepository tripPlanRepository;
    private final PlaceRepository    placeRepository;

    @Autowired
    public NominatimService(@Qualifier("nominatimWebClient") WebClient nominatimClient,
                            TripPlanRepository tripPlanRepository,
                            PlaceRepository placeRepository) {
        this.nominatimClient   = nominatimClient;
        this.tripPlanRepository = tripPlanRepository;
        this.placeRepository    = placeRepository;
    }

    /**
     * Look up “tourist attractions” in the given city, then save them
     * as Place entities on the TripPlan with the given ID.
     */
    @Transactional
    public List<Place> importPopularPlaces(Long tripPlanId, int limit) {
        TripPlan trip = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new IllegalArgumentException("TripPlan not found: " + tripPlanId));

        // 1) fetch JSON array from /search?q=tourist attractions in {city}&format=json&limit={limit}
        List<JsonNode> hits = nominatimClient.get()
                .uri(uri -> uri
                        .path("/search")
                        .queryParam("q", "tourist attractions in " + trip.getCity().getName())
                        .queryParam("format", "json")
                        .queryParam("limit", limit)
                        .queryParam("addressdetails", "0")
                        .build()
                )
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .collectList()
                .block();

        // 2) map each hit → Place entity
        List<Place> places = IntStream.range(0, hits.size())
                .mapToObj(i -> {
                    JsonNode item = hits.get(i);
                    String displayName = item.path("display_name").asText();
                    double lat = item.path("lat").asDouble();
                    double lon = item.path("lon").asDouble();

                    return Place.builder()
                            .name(displayName)
                            .type(PlaceType.ATTRACTION)
                            .description(null)
                            .location(new LatLon(lat, lon))
                            .orderIndex(i + 1)
                            .favorite(false)
                            .tripPlan(trip)
                            .build();
                })
                .toList();

        // 3) clear old places & save new ones
        trip.getPlaces().clear();
        trip.getPlaces().addAll(places);
        return placeRepository.saveAll(places);
    }
}
