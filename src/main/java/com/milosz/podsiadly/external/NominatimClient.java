package com.milosz.podsiadly.external;

import com.milosz.podsiadly.external.dto.NominatimPlaceDto;
import com.milosz.podsiadly.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class NominatimClient implements PlaceApiClient {

    private final WebClient webClient;

    public NominatimClient(
            @Value("${nominatim.base-url}") String baseUrl,
            @Value("${nominatim.user-agent}") String userAgent
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    public List<Place> fetchPlaces(PlaceType type, City city, String query) {
        Mono<NominatimPlaceDto[]> responseMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(NominatimPlaceDto[].class);

        NominatimPlaceDto[] results = responseMono.block();

        if (results == null || results.length == 0) {
            return List.of();
        }

        return List.of(results).stream()
                .map(result -> Place.builder()
                        .name(result.displayName())
                        .type(type)
                        .description("OpenStreetMap / Nominatim result")
                        .location(new LatLon(
                                Double.parseDouble(result.lat()),
                                Double.parseDouble(result.lon())
                        ))
                        .favorite(false)
                        .orderIndex(0)
                        .build())
                .toList();
    }
}
