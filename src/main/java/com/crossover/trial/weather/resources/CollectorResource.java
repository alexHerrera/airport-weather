package com.crossover.trial.weather.resources;

import com.crossover.trial.weather.domain.Airport;
import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.DataPointType;
import com.crossover.trial.weather.repositories.AirportRepository;
import com.crossover.trial.weather.repositories.WeatherDataRepository;
import com.google.gson.Gson;
import javaslang.collection.Seq;
import javaslang.collection.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.crossover.trial.weather.resources.Paths.AIRPORT;
import static com.crossover.trial.weather.resources.Paths.AIRPORTS;
import static com.crossover.trial.weather.resources.Paths.COLLECT;
import static com.crossover.trial.weather.resources.Paths.IATA_CODE;
import static com.crossover.trial.weather.resources.Paths.PING;
import static com.crossover.trial.weather.resources.Paths.POINT_TYPE;
import static com.crossover.trial.weather.resources.Paths.WEATHER;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport
 * weather collection sites via secure VPN.
 */

@Path(COLLECT)
@Controller
public class CollectorResource {
    private final static Logger log = LoggerFactory.getLogger(CollectorResource.class);

    @Inject
    private Gson gson;

    @Inject
    private AirportRepository airportRepository;

    @Inject
    private WeatherDataRepository weatherDataRepository;

    /**
     * A liveliness check for the collection endpoint.
     *
     * @return 1 if the endpoint is alive functioning, 0 otherwise
     */
    @GET
    @Path(PING)
    public Response ping() {
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    /**
     * Update the airports atmospheric information for a particular pointType with
     * json formatted data point information.
     *
     * @param iataCode      the 3 letter airport code
     * @param pointType     the point type, {@link DataPointType} for a complete list
     * @param datapointJson a json dict containing mean, first, second, thrid and count keys
     * @return HTTP Response code
     */
    @POST
    @Path(WEATHER + "/{" + IATA_CODE + "}/{" + POINT_TYPE + "}")
    public Response updateWeather(@PathParam(IATA_CODE) String iataCode,
                                  @PathParam(POINT_TYPE) String pointType, String datapointJson) {

        if (airportRepository.getAirport(iataCode).isEmpty()) {
            log.debug("updateWeather({}, {}, {}) not accepted", iataCode, pointType, datapointJson);
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        log.debug("updateWeather({}, {}, {})", iataCode, pointType, datapointJson);

        final DataPointType dataPointType = DataPointType.valueOf(pointType.toUpperCase());
        weatherDataRepository.update(iataCode, dataPointType, gson.fromJson(datapointJson, DataPoint.class));

        return Response.status(Response.Status.OK).build();
    }

    /**
     * Return a list of known airports as a json formatted list
     *
     * @return HTTP Response code and a json formatted list of IATA codes
     */
    @GET
    @Path(AIRPORTS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAirports() {
        log.debug("getAirports()");

        final Seq<Airport> airports = airportRepository.getAirports();
        final Set<String> iataCodes = airports.map(Airport::iataCode).toSet();

        return Response.status(Response.Status.OK).entity(iataCodes.toJavaList()).build();
    }

    @Path(AIRPORT + "/{" + IATA_CODE + "}")
    public Class<CollectorAirportResource> collectorAirportSubResource() {
        log.trace("collectorAirportSubResource()");
        return CollectorAirportResource.class;
    }

    //
    // Internal support methods
    //

    @GET
    @Path("/exit")
    public Response exit() {
        log.info("exit() shutting down server");

        System.exit(0);
        return Response.noContent().build();
    }
}
