package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByNameAndCountry(String name, String country);
}
