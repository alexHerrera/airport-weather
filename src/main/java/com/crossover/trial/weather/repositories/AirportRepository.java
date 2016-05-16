package com.crossover.trial.weather.repositories;

import com.crossover.trial.weather.domain.Airport;
import com.crossover.trial.weather.domain.ImmutableAirport;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.control.Option;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AirportRepository {
    private final ConcurrentHashMap<String, Airport> airportsByIataCode = new ConcurrentHashMap<>();

    public void addAirport(Airport airport) {
        airportsByIataCode.put(airport.iataCode(), airport);
    }

    public Option<Airport> getAirport(String iataCode) {
        return Option.of(airportsByIataCode.get(iataCode));
    }

    public Seq<Airport> getAirports() {
        return List.ofAll(airportsByIataCode.values()).toStream();
    }

    public void removeAirport(String iataCode) {
        airportsByIataCode.remove(iataCode);
    }

    public void addAirport(String iataCode, Double latitude, Double longitude) {
        final ImmutableAirport airport = ImmutableAirport.builder()
                .iataCode(iataCode)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        addAirport(airport);
    }

    /**
     * A dummy init method that loads hard coded data
     */
    @PostConstruct
    private void init() {
        addAirport("BOS", -71.005181, 42.364347);
        addAirport("EWR", -74.168667, 40.6925);
        addAirport("JFK", -73.778925, 40.639751);
        addAirport("LGA", -73.872608, 40.777245);
        addAirport("MMU", -74.4148747, 40.79935);
    }
}
