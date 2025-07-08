package com.milosz.podsiadly.external;

import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.model.LatLon;
import org.springframework.stereotype.Component;

@Component
public class CityApiClient {

    public City fetchCityFromExternalApi(String name, String country) {
        // TODO: Połączenie z zewnętrznym API (np. GeoDB, OpenData)
        // Tymczasowy przykład:
        return City.builder()
                .name(name)
                .country(country)
                .population(500000L)
                .region("Małopolskie")
                .timezone("Europe/Warsaw")
                .center(new LatLon(50.0647, 19.945))
                .build();
    }
}
