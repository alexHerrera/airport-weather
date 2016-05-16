package com.crossover.trial.weather.resources;

import com.crossover.trial.weather.domain.Airport;
import com.crossover.trial.weather.repositories.AirportRepository;
import com.crossover.trial.weather.repositories.WeatherDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.crossover.trial.weather.resources.Paths.IATA_CODE;
import static com.crossover.trial.weather.resources.Paths.LATITUDE;
import static com.crossover.trial.weather.resources.Paths.LONGITUDE;

@Controller
public class CollectorAirportResource {

    private final static Logger log = LoggerFactory.getLogger(CollectorAirportResource.class);

    @Inject
    private AirportRepository airportRepository;

    @Inject
    private WeatherDataRepository weatherDataRepository;

    /**
     * Retrieve airport data, including latitude and longitude for a particular airport
     *
     * @param iataCode the 3 letter airport code
     * @return an HTTP Response with a json representation of {@link Airport}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAirport(@PathParam(IATA_CODE) String iataCode) {
        log.debug("getAirport({})", iataCode);

        return airportRepository.getAirport(iataCode)
                .map(airport ->
                        Response.status(Response.Status.OK)
                                .entity(airport)
                                .build()
                ).getOrElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * Add a new airport to the known airport list.
     *
     * @param iataCode        the 3 letter airport code of the new airport
     * @param latitudeString  the airport's latitude in degrees as a string [-90, 90]
     * @param longitudeString the airport's longitude in degrees as a string [-180, 180]
     * @return HTTP Response code for the add operation
     */
    @POST
    @Path("/{" + LATITUDE + "}/{" + LONGITUDE + "}")
    public Response addAirport(@PathParam(IATA_CODE) String iataCode,
                               @PathParam(LATITUDE) String latitudeString,
                               @PathParam(LONGITUDE) String longitudeString) {
        if (airportRepository.getAirport(iataCode).isDefined()) {
            log.debug("addAirport({}, {}, {}) already exists", iataCode, latitudeString, longitudeString);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        log.debug("addAirport({}, {}, {})", iataCode, latitudeString, longitudeString);

        final Double longitude = Double.valueOf(longitudeString);
        final Double latitude = Double.valueOf(latitudeString);
        airportRepository.addAirport(iataCode, latitude, longitude);

        return Response.status(Response.Status.OK).build();
    }


    /**
     * Remove an airport from the known airport list
     *
     * @param iataCode the 3 letter airport code
     * @return HTTP Repsonse code for the delete operation
     */
    @DELETE
    public Response deleteAirport(@PathParam(IATA_CODE) String iataCode) {
        log.debug("deleteAirport({})", iataCode);

        airportRepository.removeAirport(iataCode);
        weatherDataRepository.removeStation(iataCode);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
