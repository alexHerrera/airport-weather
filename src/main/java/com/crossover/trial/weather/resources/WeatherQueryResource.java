package com.crossover.trial.weather.resources;

import com.crossover.trial.weather.domain.measurement.AtmosphericData;
import com.google.gson.Gson;
import javaslang.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.crossover.trial.weather.resources.Paths.IATA_CODE;
import static com.crossover.trial.weather.resources.Paths.PING;
import static com.crossover.trial.weather.resources.Paths.QUERY;
import static com.crossover.trial.weather.resources.Paths.RADIUS;
import static com.crossover.trial.weather.resources.Paths.WEATHER;

/**
 * The Weather App REST endpoint allows clients to query, update and check health stats. Currently, all data is
 * held in memory. The end point deploys to a single container
 *
 * @author code test administrator
 */
@Path(QUERY)
@Component
public class WeatherQueryResource {

    private static final Logger log = LoggerFactory.getLogger(WeatherQueryResource.class);

    @Inject
    private Gson gson;

    @Inject
    private WeatherQueryHandler handler;

    /**
     * Retrieve health and status information for the the query api. Returns information about how the number
     * of datapoints currently held in memory, the frequency of requests for each IATA code and the frequency of
     * requests for each radius.
     *
     * @return a JSON formatted dict with health information.
     */
    @GET
    @Path(PING)
    public String ping() {
        log.debug("ping()");

        Map<String, Object> result = handler.ping();

        return gson.toJson(result);
    }

    /**
     * Retrieve the most up to date atmospheric information from the given airport and other airports in the given
     * radius.
     *
     * @param iataCode     the three letter airport code
     * @param radiusString the radius, in km, from which to collect weather data
     * @return an HTTP Response and a list of {@link AtmosphericData} from the requested airport and
     * airports in the given radius
     */
    @GET
    @Path(WEATHER + "/{" + IATA_CODE + "}/{" + RADIUS + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response weather(@PathParam(IATA_CODE) String iataCode,
                            @PathParam(RADIUS) String radiusString) {
        log.debug("weather({}, {})", iataCode, radiusString);

        final boolean radiusStringUnset = radiusString == null || radiusString.trim().isEmpty();
        double radius = radiusStringUnset ? 0 : Double.valueOf(radiusString);

        final List<AtmosphericData> weather = handler.weather(iataCode, radius);

        return Response.status(Response.Status.OK).entity(weather.toJavaList()).build();
    }
}
