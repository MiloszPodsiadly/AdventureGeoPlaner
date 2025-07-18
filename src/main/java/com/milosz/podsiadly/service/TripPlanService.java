package com.milosz.podsiadly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.milosz.podsiadly.model.*;
import com.milosz.podsiadly.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripPlanService {

    private final TripPlanRepository tripPlanRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final SpotifyPlaylistRepository playlistRepository;
    private final NominatimService nominatimService;
    private final OsrmRoutingService routingService;

    @Transactional
    public List<Place> importPopularPlaces(Long planId, int limit) {
        // simply delegate to your NominatimService
        return nominatimService.importPopularPlaces(planId, limit);
    }

    /**
     * 🔁 Compute a driving route from first to last place in the plan,
     *     persist it, and link it into the TripPlan.
     */
    @Transactional
    public TripPlan calculateAndAssignRoute(Long planId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan not found: " + planId));

        List<Place> places = plan.getPlaces();
        if (places.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 places to compute a route");
        }

        LatLon start = places.get(0).getLocation();
        LatLon end   = places.get(places.size() - 1).getLocation();

        // call OSRM
        JsonNode resp = routingService.getRoute(start, end);
        JsonNode routeNode = resp.path("routes").get(0);

        double distance = routeNode.path("distance").asDouble();
        double duration = routeNode.path("duration").asDouble();
        String pathJson = routeNode.path("geometry").toString();

        // build & save Route
        Route route = Route.builder()
                .start(start)
                .end(end)
                .distance(distance)
                .duration(duration)
                .pathJson(pathJson)
                .build();

        route = routeRepository.save(route);

        // link into TripPlan
        plan.setRoute(route);
        return tripPlanRepository.save(plan);
    }

    @Transactional
    public TripPlan assignPlaylistToPlan(Long planId, Long spotifyPlaylistId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan not found: " + planId));

        SpotifyPlaylist playlist = playlistRepository.findById(spotifyPlaylistId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "SpotifyPlaylist not found: " + spotifyPlaylistId));

        plan.setSpotifyPlaylist(playlist);
        // other fields (places, route) stay as-is (null/empty) until you import or compute them
        return tripPlanRepository.save(plan);
    }

    public List<TripPlan> getAllPlans() {
        return tripPlanRepository.findAll();
    }

    public TripPlan getTripPlanById(Long id) {
        return tripPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan not found with id: " + id));
    }

    public List<TripPlan> getPlansByUserId(Long userId) {
        return tripPlanRepository.findByUserId(userId);
    }

    @Transactional
    public TripPlan createTripPlan(Long userId, Long cityId, TripPlan plan, Long playlistId) {
        validate(plan);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found"));

        SpotifyPlaylist playlist = null;
        if (playlistId != null) {
            playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new EntityNotFoundException("SpotifyPlaylist not found with id: " + playlistId));
        }

        plan.setUser(user);
        plan.setCity(city);
        plan.setSpotifyPlaylist(playlist);
        plan.setRoute(null);

        return tripPlanRepository.save(plan);
    }

    @Transactional
    public TripPlan updateTripPlan(Long id, TripPlan updated, Long newPlaylistId) {
        validate(updated);
        TripPlan existing = getTripPlanById(id);

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDate(updated.getDate());

        if (newPlaylistId != null) {
            SpotifyPlaylist playlist = playlistRepository.findById(newPlaylistId)
                    .orElseThrow(() -> new EntityNotFoundException("SpotifyPlaylist not found with id: " + newPlaylistId));
            existing.setSpotifyPlaylist(playlist);
        } else {
            existing.setSpotifyPlaylist(null);
        }

        return tripPlanRepository.save(existing);
    }

    @Transactional
    public void deleteTripPlan(Long id) {
        if (!tripPlanRepository.existsById(id)) {
            throw new EntityNotFoundException("TripPlan not found with id: " + id);
        }
        tripPlanRepository.deleteById(id);
    }

    @Transactional
    public TripPlan assignRouteToPlan(Long planId, Route route) {
        TripPlan plan = getTripPlanById(planId);
        plan.setRoute(routeRepository.save(route));
        return tripPlanRepository.save(plan);
    }

    private void validate(TripPlan plan) {
        if (plan == null) throw new IllegalArgumentException("TripPlan cannot be null");
        if (StringUtils.isBlank(plan.getTitle())) throw new IllegalArgumentException("Title is required");
        if (plan.getDate() == null || plan.getDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Date must be today or later");
    }
}
