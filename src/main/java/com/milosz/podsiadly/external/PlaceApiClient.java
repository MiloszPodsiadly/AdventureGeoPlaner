package com.milosz.podsiadly.external;

import com.milosz.podsiadly.model.City;
import com.milosz.podsiadly.model.Place;
import com.milosz.podsiadly.model.PlaceType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Klient do zewnętrznego API miejsc (np. OpenTripMap, Foursquare, Google).
 */
public interface PlaceApiClient {

    /**
     * Wyszukuje miejsca w danym mieście i typie (np. muzea, restauracje) z API zewnętrznego.
     *
     * @param type   typ miejsca (np. MUSEUM, RESTAURANT, CUSTOM)
     * @param city   obiekt miasta z lokalizacją centrum
     * @param query  opcjonalna fraza wyszukiwania (np. "pierogi", "park")
     * @return lista miejsc gotowych do zapisania
     */
    List<Place> fetchPlaces(PlaceType type, City city, String query);
}
