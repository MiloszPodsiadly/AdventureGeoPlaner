package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByTripPlan(TripPlan tripPlan);

    // Możliwości rozwoju:
    List<Place> findByTripPlanId(Long tripPlanId);
    List<Place> findByTripPlan_City_Id(Long cityId);

    List<Place> findByTripPlanIdOrderByOrderIndexAsc(Long tripPlanId);

}
