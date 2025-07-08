package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.CityEventDto;
import com.milosz.podsiadly.mapper.CityEventMapper;
import com.milosz.podsiadly.model.CityEvent;
import com.milosz.podsiadly.service.CityEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/city-events")
@RequiredArgsConstructor
public class CityEventController {

    private final CityEventService cityEventService;

    // 🔎 GET all
    @GetMapping
    public ResponseEntity<List<CityEventDto>> getAllEvents() {
        List<CityEvent> events = cityEventService.getAllEvents();
        return ResponseEntity.ok(CityEventMapper.mapToDtoList(events));
    }

    // 🔎 GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<CityEventDto> getEventById(@PathVariable Long id) {
        CityEvent event = cityEventService.getEventById(id);
        return ResponseEntity.ok(CityEventMapper.mapToDto(event));
    }

    // 🔎 GET by City ID
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<CityEventDto>> getEventsByCity(@PathVariable Long cityId) {
        List<CityEvent> events = cityEventService.getEventsByCity(cityId);
        return ResponseEntity.ok(CityEventMapper.mapToDtoList(events));
    }

    // ➕ POST (create)
    @PostMapping
    public ResponseEntity<CityEventDto> createEvent(@RequestBody CityEvent event) {
        CityEvent created = cityEventService.createEvent(event);
        return ResponseEntity.ok(CityEventMapper.mapToDto(created));
    }

    // ✏️ PUT (update)
    @PutMapping("/{id}")
    public ResponseEntity<CityEventDto> updateEvent(@PathVariable Long id, @RequestBody CityEvent updatedEvent) {
        CityEvent updated = cityEventService.updateEvent(id, updatedEvent);
        return ResponseEntity.ok(CityEventMapper.mapToDto(updated));
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        cityEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
