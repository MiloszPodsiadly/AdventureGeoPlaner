package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.CityDto;
import com.milosz.podsiadly.model.City;

public class CityMapper {

    public static CityDto toDto(City c) {
        return new CityDto(
                c.getId(),
                c.getName(),
                c.getCountry(),
                c.getPopulation(),
                c.getRegion(),
                c.getTimezone(),
                c.getCenter().getLat(),
                c.getCenter().getLon()
        );
    }

    public static City fromDto(CityDto dto) {
        return City.builder()
                .name(dto.name())
                .country(dto.country())
                .population(dto.population())
                .region(dto.region())
                .timezone(dto.timezone())
                .center(new com.milosz.podsiadly.model.LatLon(dto.lat(), dto.lng()))
                .build();
    }
}
