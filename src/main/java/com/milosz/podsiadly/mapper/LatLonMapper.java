package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.LatLonDto;
import com.milosz.podsiadly.model.LatLon;

public class LatLonMapper {

    public static LatLonDto mapToDto(LatLon latLon) {
        if (latLon == null) return new LatLonDto(0, 0);
        return new LatLonDto(latLon.getLat(), latLon.getLon());
    }

    public static LatLon fromDto(LatLonDto dto) {
        if (dto == null) return new LatLon(0, 0);
        return new LatLon(dto.lat(), dto.lon());
    }
}
