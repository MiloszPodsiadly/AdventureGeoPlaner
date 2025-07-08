package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserId(Long userId);
    List<Recommendation> findByPlaceId(Long placeId);
}
