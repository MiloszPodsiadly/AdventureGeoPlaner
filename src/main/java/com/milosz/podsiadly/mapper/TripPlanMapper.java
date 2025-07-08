package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.TripPlanDto;
import com.milosz.podsiadly.model.TripPlan;

import java.util.List;

public class TripPlanMapper {

    public static TripPlanDto mapToDto(TripPlan plan) {
        return new TripPlanDto(
                plan.getId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getDate(),
                plan.getCity() != null ? plan.getCity().getId() : null,
                plan.getUser() != null ? plan.getUser().getId() : null,
                plan.getRoute() != null ? plan.getRoute().getId() : null,
                plan.getSpotifyPlaylist() != null ? plan.getSpotifyPlaylist().getId() : null,
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    public static List<TripPlanDto> mapToDtoList(List<TripPlan> plans) {
        return plans.stream()
                .map(TripPlanMapper::mapToDto)
                .toList();
    }
}
