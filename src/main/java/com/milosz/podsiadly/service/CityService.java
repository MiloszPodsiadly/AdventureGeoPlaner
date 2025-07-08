package com.milosz.podsiadly.service;

import com.milosz.podsiadly.external.CityApiClient;
import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.repository.CityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;
    private final CityApiClient cityApiClient;

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    public Page<City> getCities(Pageable pageable) {
        return cityRepository.findAll(pageable);
    }
    //public List<City> searchCities(String namePart) {
    //    return cityRepository.findByNameContainingIgnoreCase(namePart);
    //}

    public City getCityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("City ID cannot be null.");
        }

        return cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + id));
    }

    @Transactional
    public City createCity(City city) {
        validateCity(city);
        return cityRepository.save(city);
    }

    @Transactional
    public City updateCity(Long id, City updatedCity) {
        if (id == null) {
            throw new IllegalArgumentException("City ID cannot be null.");
        }

        validateCity(updatedCity);

        City existing = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + id));

        existing.setName(updatedCity.getName());
        existing.setCountry(updatedCity.getCountry());
        existing.setPopulation(updatedCity.getPopulation());
        existing.setRegion(updatedCity.getRegion());
        existing.setTimezone(updatedCity.getTimezone());
        existing.setCenter(updatedCity.getCenter());

        return cityRepository.save(existing);
    }

    @Transactional
    public void deleteCity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("City ID cannot be null.");
        }

        if (!cityRepository.existsById(id)) {
            throw new EntityNotFoundException("City not found with id: " + id);
        }

        cityRepository.deleteById(id);
    }

    public City getOrFetchCity(String name, String country) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(country)) {
            throw new IllegalArgumentException("City name and country cannot be empty.");
        }

        return cityRepository.findAll().stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()) && country.equalsIgnoreCase(c.getCountry()))
                .findFirst()
                .orElseGet(() -> {
                    City cityFromApi = cityApiClient.fetchCityFromExternalApi(name, country);

                    validateCity(cityFromApi); // sprawdzamy nawet to, co przyszło z API

                    return cityRepository.save(cityFromApi);
                });
    }

    private void validateCity(City city) {
        if (city == null) throw new IllegalArgumentException("City cannot be null.");
        if (StringUtils.isBlank(city.getName())) throw new IllegalArgumentException("City name is required.");
        if (StringUtils.isBlank(city.getCountry())) throw new IllegalArgumentException("Country is required.");
        if (city.getCenter() == null) throw new IllegalArgumentException("Center coordinates are required.");
    }
}
