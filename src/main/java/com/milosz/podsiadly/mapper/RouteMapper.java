    package com.milosz.podsiadly.mapper;

    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.milosz.podsiadly.dto.LatLonDto;
    import com.milosz.podsiadly.dto.RouteDto;
    import com.milosz.podsiadly.model.LatLon;
    import com.milosz.podsiadly.model.Route;

    import java.util.ArrayList;
    import java.util.List;

    public class RouteMapper {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        public static RouteDto mapToDto(Route route) {
            return new RouteDto(
                    route.getId(),
                    route.getDistance(),
                    route.getDuration(),
                    new LatLonDto(route.getStart().getLat(), route.getStart().getLon()),
                    new LatLonDto(route.getEnd().getLat(), route.getEnd().getLon()),
                    route.getPathJson()
            );
        }

        public static List<List<Double>> mapToCoordinatesList(Route route) {
            try {
                List<LatLon> path = objectMapper.readValue(
                        route.getPathJson(),
                        new TypeReference<>() {}
                );

                List<List<Double>> coordinates = new ArrayList<>();
                for (LatLon latLon : path) {
                    coordinates.add(List.of(latLon.getLon(), latLon.getLat())); // GeoJSON = [lng, lat]
                }
                return coordinates;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse route JSON path", e);
            }
        }

        public static Route fromDto(RouteDto dto) {
            return Route.builder()
                    .id(dto.id())
                    .distance(dto.distance())
                    .duration(dto.duration())
                    .start(new LatLon(dto.start().lat(), dto.start().lon()))
                    .end(new LatLon(dto.end().lat(), dto.end().lon()))
                    .pathJson(dto.pathJson())
                    .build();
        }
    }
