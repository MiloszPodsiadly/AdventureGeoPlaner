package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.model.CityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CityEventRepository extends JpaRepository<CityEvent, Long> {
    List<CityEvent> findByCity(City city);
}
