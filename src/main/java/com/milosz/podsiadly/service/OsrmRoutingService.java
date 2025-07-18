
package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.LatLon;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service

public class OsrmRoutingService {

    private final WebClient osrmClient;

    public OsrmRoutingService(@Qualifier("osrmWebClient") WebClient osrmClient) {
        this.osrmClient = osrmClient;
    }

    /**
     * Returns the raw OSRM JSON with distance, duration, and geometry.
     */
    public JsonNode getRoute(LatLon start, LatLon end) {
        return osrmClient.get()
                .uri(uri -> uri
                        .path("/route/v1/driving/{startLon},{startLat};{endLon},{endLat}")
                        .queryParam("overview", "full")
                        .queryParam("geometries", "geojson")
                        .build(
                                start.getLon(), start.getLat(),
                                end.getLon(),   end.getLat()
                        )
                )
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
