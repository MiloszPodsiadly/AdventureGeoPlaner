package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.LatLonDto;
import com.milosz.podsiadly.dto.PlaceDto;
import com.milosz.podsiadly.model.LatLon;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.PlaceType;
import com.milosz.podsiadly.model.TripPlan;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaceMapper {

    public PlaceDto mapToDto(Place place) {
        return new PlaceDto(
                place.getId(),
                place.getName(),
                place.getDescription(),
                place.getType().name(),
                place.getLocation() != null
                        ? new LatLonDto(place.getLocation().getLat(), place.getLocation().getLon())
                        : null,
                place.getOrderIndex(),
                place.isFavorite(),
                place.getTripPlan() != null ? place.getTripPlan().getId() : null
        );
    }

    public List<PlaceDto> mapToDtoList(List<Place> places) {
        return places.stream()
                .map(this::mapToDto)
                .toList();
    }

    public Place mapToEntity(PlaceDto dto) {
        return Place.builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .type(PlaceType.valueOf(dto.type()))
                .location(dto.location() != null
                        ? new LatLon(dto.location().lat(), dto.location().lon())
                        : null)
                .orderIndex(dto.orderIndex() != null ? dto.orderIndex() : 0)
                .favorite(dto.isFavorite())
                .tripPlan(dto.tripPlanId() != null
                        ? TripPlan.builder().id(dto.tripPlanId()).build()
                        : null)
                .build();
    }
}
