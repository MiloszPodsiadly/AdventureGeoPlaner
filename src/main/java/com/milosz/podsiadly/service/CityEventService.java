package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.model.CityEvent;
import com.milosz.podsiadly.repository.CityEventRepository;
import com.milosz.podsiadly.repository.CityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityEventService {

    private final CityEventRepository cityEventRepository;
    private final CityRepository cityRepository;

    // ✅ Zwraca wszystkie eventy (np. do listy w UI)
    public List<CityEvent> getAllEvents() {
        return cityEventRepository.findAll();
    }

    // ✅ Zwraca event po ID
    public CityEvent getEventById(Long id) {
        return cityEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CityEvent not found with id: " + id));
    }

    // ✅ Tworzy nowy event (np. z API)
    @Transactional
    public CityEvent createEvent(CityEvent event) {
        validate(event);
        return cityEventRepository.save(event);
    }

    // ✅ Aktualizacja eventu (jeśli jest ręcznie edytowany)
    @Transactional
    public CityEvent updateEvent(Long id, CityEvent updatedEvent) {
        CityEvent existing = getEventById(id);
        validate(updatedEvent);

        existing.setTitle(updatedEvent.getTitle());
        existing.setDescription(updatedEvent.getDescription());
        existing.setCategory(updatedEvent.getCategory());
        existing.setStartTime(updatedEvent.getStartTime());
        existing.setEndTime(updatedEvent.getEndTime());
        existing.setSource(updatedEvent.getSource());
        existing.setExternalUrl(updatedEvent.getExternalUrl());
        existing.setLocation(updatedEvent.getLocation());

        return cityEventRepository.save(existing);
    }

    // ✅ Usuwa event po ID
    @Transactional
    public void deleteEvent(Long id) {
        if (!cityEventRepository.existsById(id)) {
            throw new EntityNotFoundException("CityEvent not found with id: " + id);
        }
        cityEventRepository.deleteById(id);
    }

    // ✅ Pobieranie eventów dla danego miasta
    public List<CityEvent> getEventsByCity(Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + cityId));
        return cityEventRepository.findByCity(city);
    }

    // 🛡️ Walidacja – zabezpieczenie przed nullami i złymi danymi
    private void validate(CityEvent event) {
        if (event == null) throw new IllegalArgumentException("CityEvent cannot be null");
        if (StringUtils.isBlank(event.getTitle())) throw new IllegalArgumentException("Event title is required");
        if (event.getStartTime() == null || event.getEndTime() == null)
            throw new IllegalArgumentException("Event must have a start and end time");
        if (event.getCity() == null) throw new IllegalArgumentException("Event must be linked to a city");
        if (event.getStartTime().isAfter(event.getEndTime()))
            throw new IllegalArgumentException("Start time cannot be after end time");
    }
}
