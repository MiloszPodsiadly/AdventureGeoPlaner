package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.LatLonDto;
import com.milosz.podsiadly.dto.PlaceDto;
import com.milosz.podsiadly.model.LatLon;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.PlaceType;

import java.util.List;
import java.util.stream.Collectors;

public class PlaceMapper {

    /** Entity → DTO */
    public static PlaceDto toDto(Place p) {
        return new PlaceDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getType().name(),
                new LatLonDto(p.getLocation().getLat(), p.getLocation().getLon()),
                p.getOrderIndex(),
                p.isFavorite(),
                p.getTripPlan() != null ? p.getTripPlan().getId() : null
        );
    }

    /** Map a list of entities → list of DTOs */
    public static List<PlaceDto> toDtoList(List<Place> places) {
        return places.stream()
                .map(PlaceMapper::toDto)
                .collect(Collectors.toList());
    }

    /** DTO → Entity (for creation/update) */
    public static Place fromDto(PlaceDto dto) {
        PlaceType type = dto.type() != null
                ? PlaceType.valueOf(dto.type())
                : PlaceType.ATTRACTION;

        LatLon latLon = new LatLon(
                dto.location().lat(),
                dto.location().lon()
        );

        return Place.builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .type(type)
                .location(latLon)
                .orderIndex(dto.orderIndex())
                .favorite(dto.isFavorite())
                .build();
    }
}
