package com.crossover.trial.weather.resources;

import com.crossover.trial.weather.WeatherServer;
import com.crossover.trial.weather.domain.Airport;
import com.google.gson.Gson;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(WeatherServer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CollectorResourceIntegrationTest {

    @Inject
    private CollectorAirportResource airportResource;

    @Inject
    private CollectorResource collectorResource;

    @Test
    public void addingWeatherForNonExistentAirportReturns406() throws Exception {
        Response response = collectorResource.updateWeather("FOO", "", "");
        assertThat(response.getStatus()).isEqualTo(406);
    }

    @Test
    public void getAirportsShouldReturnListOfAirports() {
        airportResource.addAirport("FOO", "49", "11");
        Response response = collectorResource.getAirports();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull().isInstanceOf(java.util.List.class);

        ArrayList<String> entity = (ArrayList<String>) response.getEntity();

        assertThat(entity).contains("FOO");
    }

}