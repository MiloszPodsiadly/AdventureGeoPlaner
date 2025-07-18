package com.milosz.podsiadly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${nominatim.user-agent}")
    private String userAgent;

    @Bean("nominatimWebClient")
    public WebClient nominatimClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }

    @Bean("osrmWebClient")
    public WebClient osrmClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://router.project-osrm.org")
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }
}