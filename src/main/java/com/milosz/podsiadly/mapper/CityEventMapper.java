package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.CityEventDto;
import com.milosz.podsiadly.model.CityEvent;

import java.util.List;

public class CityEventMapper {

    public static CityEventDto mapToDto(CityEvent event) {
        return new CityEventDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getSource(),
                event.getExternalUrl(),
                event.getCity() != null ? event.getCity().getId() : null,
                event.getLocation() != null ? event.getLocation().getLat() : 0,
                event.getLocation() != null ? event.getLocation().getLon() : 0,
                event.getStartTime(),
                event.getEndTime()
        );
    }

    public static List<CityEventDto> mapToDtoList(List<CityEvent> events) {
        return events.stream()
                .map(CityEventMapper::mapToDto)
                .toList();
    }
}
