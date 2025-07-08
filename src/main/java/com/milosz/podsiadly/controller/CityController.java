package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.CityDto;
import com.milosz.podsiadly.mapper.CityMapper;
import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    // 🔎 GET all
    @GetMapping
    public ResponseEntity<List<CityDto>> getAllCities() {
        return ResponseEntity.ok(
                cityService.getAllCities().stream()
                        .map(CityMapper::mapToDto)
                        .toList()
        );
    }

    // 🔎 GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<CityDto> getCityById(@PathVariable Long id) {
        City city = cityService.getCityById(id);
        return ResponseEntity.ok(CityMapper.mapToDto(city));
    }

    // ➕ POST
    @PostMapping
    public ResponseEntity<CityDto> createCity(@RequestBody City city) {
        City created = cityService.createCity(city);
        return ResponseEntity.ok(CityMapper.mapToDto(created));
    }

    // ✏️ PUT
    @PutMapping("/{id}")
    public ResponseEntity<CityDto> updateCity(@PathVariable Long id, @RequestBody City updatedCity) {
        City city = cityService.updateCity(id, updatedCity);
        return ResponseEntity.ok(CityMapper.mapToDto(city));
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    // 🧠 GET or fetch from external API
    @GetMapping("/resolve")
    public ResponseEntity<CityDto> getOrFetchCity(@RequestParam String name, @RequestParam String country) {
        City city = cityService.getOrFetchCity(name, country);
        return ResponseEntity.ok(CityMapper.mapToDto(city));
    }
}
