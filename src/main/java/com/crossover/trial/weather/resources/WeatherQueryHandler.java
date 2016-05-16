package com.crossover.trial.weather.resources;

import com.crossover.trial.weather.domain.Airport;
import com.crossover.trial.weather.domain.measurement.AtmosphericData;
import com.crossover.trial.weather.lib.EventCounter;
import com.crossover.trial.weather.lib.GeoCalculations;
import com.crossover.trial.weather.lib.TimestampFactory;
import com.crossover.trial.weather.repositories.AirportRepository;
import com.crossover.trial.weather.repositories.WeatherDataRepository;
import javaslang.collection.List;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static com.crossover.trial.weather.resources.Paths.IATA_CODE;
import static com.crossover.trial.weather.resources.Paths.RADIUS;
import static com.crossover.trial.weather.resources.Paths.WEATHER;

@Component
class WeatherQueryHandler {

    private static final int MILLISECONDS_PER_DAY = 86400000;

    @Inject
    private WeatherDataRepository weatherDataRepository;

    @Inject
    private AirportRepository airportRepository;

    @Inject
    private GeoCalculations geoCalculations;

    @Inject
    private EventCounter<Airport> requestFrequency;

    @Inject
    private EventCounter<Double> radiusFrequency;

    @Inject
    private TimestampFactory timestampFactory;

    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();

        result.put("datasize", getCountOfDataUpdatedSinceADayAgo());

        result.put("iata_freq", getAirportFractions());

        result.put("radius_freq", getRadiusHistogram());

        return result;
    }

    /**
     * Retrieve the most up to date atmospheric information from the given airport and other airports in the given
     * radius.
     *
     * @param iataCode the three letter airport code
     * @param radius   the radius, in km, from which to collect weather data
     * @return an HTTP Response and a list of {@link AtmosphericData} from the requested airport and
     * airports in the given radius
     */
    @GET
    @Path(WEATHER + "/{" + IATA_CODE + "}/{" + RADIUS + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AtmosphericData> weather(String iataCode, double radius) {
        updateRequestFrequency(iataCode, radius);

        List<AtmosphericData> retval;
        if (radius == 0) {
            return weatherDataRepository
                    .getWeatherDataFor(iataCode)
                    .toList();
        } else {
            return airportRepository
                    .getAirport(iataCode)
                    .toList()
                    .flatMap(centerAirport -> airportRepository
                            .getAirports()
                            .filter(otherAirport -> geoCalculations
                                    .calculateDistance(otherAirport, centerAirport) <= radius)
                            .map(Airport::iataCode)
                            .flatMap(weatherDataRepository::getWeatherDataFor)
                            .toList());
        }
    }

    private int getCountOfDataUpdatedSinceADayAgo() {
        final long oneDayAgo = timestampFactory.getCurrentTimestamp() - MILLISECONDS_PER_DAY;

        return weatherDataRepository
                .getWeatherData()
                .count(data -> data.lastUpdateTime() > oneDayAgo);
    }

    private Map<String, Double> getAirportFractions() {
        Map<String, Double> freq = new HashMap<>();
        airportRepository.getAirports().forEach(airport ->
                freq.put(airport.iataCode(), requestFrequency.fractionOf(airport))
        );
        return freq;
    }

    private int[] getRadiusHistogram() {
        int maxRange = radiusFrequency.events().max().getOrElse(1000.0).intValue();
        int binSize = 10;

        final int binCount = maxRange / binSize + 1;

        int[] hist = new int[binCount];

        radiusFrequency
                .stream()
                .forEach(tuple -> {
                            final Double radius = tuple._1();
                            int binIndex = radius.intValue() / 10;

                            final int radiusFrequency = tuple._2().get();
                            hist[binIndex] += radiusFrequency;
                        }
                );

        return hist;
    }

    private void updateRequestFrequency(String iataCode, Double radius) {
        airportRepository
                .getAirport(iataCode)
                .forEach(airport -> {
                            requestFrequency.increment(airport);
                            radiusFrequency.increment(radius);
                        }
                );
    }
}
