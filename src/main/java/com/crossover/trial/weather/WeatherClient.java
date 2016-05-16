package com.crossover.trial.weather;

import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.ImmutableDataPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.ServiceLoader;

/**
 * A reference implementation for the weather client. Consumers of the REST API can look at WeatherClient
 * to understand API semantics. This existing client populates the REST endpoint with dummy data useful for
 * testing.
 */

public class WeatherClient {

    private static final String BASE_URI = "http://localhost:9090";

    private final Gson gson;

    /**
     * end point for read queries
     */
    private WebTarget query;

    /**
     * end point to supply updates
     */
    private WebTarget collect;

    public WeatherClient() {
        Client client = ClientBuilder.newClient();
        gson = gsonBuilder();
        query = client.target(BASE_URI + "/query");
        collect = client.target(BASE_URI + "/collect");
    }

    public void pingCollect() {
        WebTarget path = collect.path("/ping");
        Response response = path.request().get();
        System.out.print("collect.ping: " + response.readEntity(String.class) + "\n");
    }

    public void query(String iata) {
        WebTarget path = query.path("/weather/" + iata + "/0");
        Response response = path.request().get();
        System.out.println("query." + iata + ".0: " + response.readEntity(String.class));
    }

    public void pingQuery() {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        System.out.println("query.ping: " + response.readEntity(String.class));
    }

    public void populate(String pointType, int first, int last, int mean, int median, int count) {
        WebTarget path = collect.path("/weather/BOS/" + pointType);
        DataPoint data = ImmutableDataPoint.builder()
                .first(first).last(last).mean(mean).median(median).count(count)
                .build();
        path.request().post(Entity.entity(gson.toJson(data), "application/json"));
    }

    public void exit() {
        try {
            collect.path("/exit").request().get();
        } catch (Throwable t) {
            // swallow
        }
    }

    public static void main(String[] args) {
        WeatherClient wc = new WeatherClient();

        wc.pingCollect();
        wc.populate("wind", 0, 10, 6, 4, 20);

        wc.query("BOS");
        wc.query("JFK");
        wc.query("EWR");
        wc.query("LGA");
        wc.query("MMU");

        wc.pingQuery();
        wc.exit();
        System.out.print("complete");
        System.exit(0);
    }

    private static Gson gsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class)) {
            gsonBuilder.registerTypeAdapterFactory(factory);
        }

        return gsonBuilder.create();
    }
}
