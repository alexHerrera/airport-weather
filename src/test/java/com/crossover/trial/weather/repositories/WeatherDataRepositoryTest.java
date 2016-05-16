package com.crossover.trial.weather.repositories;

import com.crossover.trial.weather.domain.measurement.AtmosphericData;
import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.DataPointType;
import javaslang.control.Option;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WeatherDataRepositoryTest {

    @Mock
    private StationDataRepository stationDataRepository;

    @Spy
    private Provider<StationDataRepository> dataRepositoryProvider = new Provider<StationDataRepository>() {
        @Override
        public StationDataRepository get() {
            return stationDataRepository;
        }
    };

    @InjectMocks
    private WeatherDataRepository weatherDataRepository;

    @Test
    public void returnsEmptyWeatherDataAfterInitialization() {
        assertThat(weatherDataRepository.getWeatherData().toList()).isEmpty();
    }

    @Test
    public void returnsEmptyElementAfterInitialization() {
        assertThat(weatherDataRepository.getWeatherDataFor("foo")).isEmpty();
    }

    @Test
    public void shouldCreateDataRepositoryAndAddNewDataWhenUpdating() {
        DataPoint dataPoint = mock(DataPoint.class);

        weatherDataRepository.update("foo", DataPointType.WIND, dataPoint);

        assertThat(weatherDataRepository.getWeatherData().size()).isEqualTo(1);
        verify(stationDataRepository).update(DataPointType.WIND, dataPoint);
    }

    @Test
    public void shouldReuseDataRepositoryAndAddNewDataWhenUpdatingAnExistingRepository() {
        DataPoint dataPoint = mock(DataPoint.class);
        weatherDataRepository.update("foo", DataPointType.WIND, dataPoint);

        weatherDataRepository.update("foo", DataPointType.HUMIDITY, dataPoint);

        assertThat(weatherDataRepository.getWeatherData().size()).isEqualTo(1);
        verify(dataRepositoryProvider).get();
        verify(stationDataRepository).update(DataPointType.WIND, dataPoint);
        verify(stationDataRepository).update(DataPointType.HUMIDITY, dataPoint);
    }

    @Test
    public void getWeatherDataForReturnsDataIfStationIsKnown() {
        DataPoint dataPoint = mock(DataPoint.class);
        weatherDataRepository.update("foo", DataPointType.WIND, dataPoint);
        AtmosphericData atmosphericData = mock(AtmosphericData.class);
        when(stationDataRepository.toData()).thenReturn(atmosphericData);

        final Option<AtmosphericData> result = weatherDataRepository.getWeatherDataFor("foo");

        assertThat(result).contains(atmosphericData);
    }

    @Test
    public void removeStationIgnoresIfStationDoesNotExist() {
        weatherDataRepository.removeStation("bar");

        assertThat(weatherDataRepository.getWeatherData().toList()).isEmpty();
    }

    @Test
    public void removeStationRemovesExistingStationRepository() {
        DataPoint dataPoint = mock(DataPoint.class);
        weatherDataRepository.update("foo", DataPointType.WIND, dataPoint);

        weatherDataRepository.removeStation("foo");

        assertThat(weatherDataRepository.getWeatherData().toList()).isEmpty();
    }
}