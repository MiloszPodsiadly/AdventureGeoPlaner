package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.LatLonDto;
import com.milosz.podsiadly.model.LatLon;
import com.milosz.podsiadly.service.LatLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class LatLngController {

    private final LatLogService latLogService;

    /**
     * 🔎 Pobiera współrzędne GPS dla danego adresu
     */
    @GetMapping("/geocode")
    public ResponseEntity<LatLonDto> geocode(@RequestParam String address) {
        return latLogService.getCoordinatesForAddress(address)
                .map(coord -> ResponseEntity.ok(new LatLonDto(coord.getLat(), coord.getLon())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 🔁 Oblicza dystans między dwoma punktami GPS
     */
    @GetMapping("/distance")
    public ResponseEntity<Double> distance(
            @RequestParam double fromLat,
            @RequestParam double fromLng,
            @RequestParam double toLat,
            @RequestParam double toLng) {

        LatLon from = new LatLon(fromLat, fromLng);
        LatLon to = new LatLon(toLat, toLng);

        double distanceKm = latLogService.calculateDistanceKm(from, to);
        return ResponseEntity.ok(distanceKm);
    }
}
