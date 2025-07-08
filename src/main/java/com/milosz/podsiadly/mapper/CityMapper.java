package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.CityDto;
import com.milosz.podsiadly.model.City;

import java.util.List;

public class CityMapper {

    public static CityDto mapToDto(City city) {
        return new CityDto(
                city.getId(),
                city.getName(),
                city.getCountry(),
                city.getPopulation(),
                city.getRegion(),
                city.getTimezone(),
                city.getCenter().getLat(),
                city.getCenter().getLon()
        );
    }

    public static List<CityDto> mapToDtoList(List<City> cities) {
        return cities.stream()
                .map(CityMapper::mapToDto)
                .toList();
    }
}
