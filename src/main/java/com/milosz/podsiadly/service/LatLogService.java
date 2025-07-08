package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.LatLon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/**
 * Service pomocniczy do obsługi geolokalizacji, map, dystansów i integracji z API zewnętrznymi (np. Nominatim, Google).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LatLogService {

    private final WebClient webClient;

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    /**
     * Geokodowanie – pobiera współrzędne dla podanego adresu (nazwa ulicy, miasta itp.)
     */
    public Optional<LatLon> getCoordinatesForAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .toUri();

        try {
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(NominatimResponse.class)
                    .timeout(Duration.ofSeconds(4))
                    .retry(1)
                    .next()
                    .map(resp -> {
                        log.info("Coordinates for '{}': lat={}, lon={}", address, resp.lat(), resp.lon());
                        return new LatLon(Double.parseDouble(resp.lat()), Double.parseDouble(resp.lon()));
                    })
                    .blockOptional();
        } catch (Exception e) {
            log.error("Failed to fetch coordinates for address '{}'", address, e);
            return Optional.empty();
        }
    }

    /**
     * Oblicza dystans w kilometrach między dwoma punktami GPS.
     */
    public double calculateDistanceKm(LatLon from, LatLon to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both coordinates must be provided");
        }

        final int R = 6371; // promień Ziemi w km
        double latDistance = Math.toRadians(to.getLat() - from.getLat());
        double lonDistance = Math.toRadians(to.getLon() - from.getLon());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.getLat())) * Math.cos(Math.toRadians(to.getLat()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // DTO na odpowiedź z API Nominatim
    private record NominatimResponse(String lat, String lon) {}
}
