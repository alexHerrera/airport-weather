package com.crossover.trial.weather.repositories;

import com.crossover.trial.weather.domain.Airport;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AirportRepositoryTest {

    @InjectMocks
    private AirportRepository repository;

    @Test
    public void shouldBeEmptyAfterInitialization() {
        assertThat(repository.getAirports().toList()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyResultIfAirportIsNotFound() {
        assertThat(repository.getAirport("foo")).isEmpty();
    }

    @Test
    public void anAddedAirportShouldBePersisted() {
        repository.addAirport("foo", 49.0, 11.0);

        assertThat(repository.getAirport("foo")).isNotEmpty().have(new Condition<Airport>() {
            @Override
            public boolean matches(Airport airport) {
                return airport.iataCode().equals("foo") &&
                        airport.latitude() == 49.0 &&
                        airport.longitude() == 11.0;
            }
        });
    }

    @Test
    public void removingANonExistentAirportShouldBeIgnored() {
        repository.removeAirport("bar");

        assertThat(repository.getAirports().toList()).isEmpty();
    }

    @Test
    public void removedAirportShouldDisappearFromRepository() {
        repository.addAirport("foo", 49.0, 11.0);

        repository.removeAirport("foo");

        assertThat(repository.getAirport("foo")).isEmpty();
    }

}