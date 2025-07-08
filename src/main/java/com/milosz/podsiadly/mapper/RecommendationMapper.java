package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.RecommendationDto;
import com.milosz.podsiadly.model.Recommendation;

import java.util.List;

public class RecommendationMapper {

    public static RecommendationDto mapToDto(Recommendation recommendation) {
        return new RecommendationDto(
                recommendation.getId(),
                recommendation.getReason(),
                recommendation.getSource(),
                recommendation.getUser() != null ? recommendation.getUser().getId() : null,
                recommendation.getPlace() != null ? recommendation.getPlace().getId() : null,
                recommendation.getCreatedAt()
        );
    }

    public static List<RecommendationDto> mapToDtoList(List<Recommendation> recommendations) {
        return recommendations.stream()
                .map(RecommendationMapper::mapToDto)
                .toList();
    }
}
