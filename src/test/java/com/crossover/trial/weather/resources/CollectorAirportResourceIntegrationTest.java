package com.crossover.trial.weather.resources;

import com.crossover.trial.weather.WeatherServer;
import com.crossover.trial.weather.domain.Airport;
import com.crossover.trial.weather.domain.measurement.AtmosphericData;
import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.ImmutableDataPoint;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(WeatherServer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CollectorAirportResourceIntegrationTest {

    @Inject
    private CollectorAirportResource airportResource;

    @Inject
    private Gson gson;

    @Test
    public void gettigNonExistentAirportReturns404() throws Exception {
        Response response = airportResource.getAirport("FOO");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void gettingExistingAirportReturnsData() {
        airportResource.addAirport("FOO", "49", "11");

        Response response = airportResource.getAirport("FOO");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull().isInstanceOf(Airport.class);

        Airport airport = (Airport) response.getEntity();
        assertThat(airport.iataCode()).isEqualTo("FOO");
        assertThat(airport.latitude()).isEqualTo(49.0);
        assertThat(airport.longitude()).isEqualTo(11);
    }

    @Test
    public void removingExistingAirportShouldWork() {
        airportResource.addAirport("FOO", "49", "11");

        airportResource.deleteAirport("FOO");

        Response response = airportResource.getAirport("FOO");
        assertThat(response.getStatus()).isEqualTo(404);
    }

}