package com.crossover.trial.weather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ServiceLoader;

@Configuration
public class WeatherServerConfiguration {

    @Bean
    public Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class)) {
            gsonBuilder.registerTypeAdapterFactory(factory);
        }

        return gsonBuilder.create();
    }
}
