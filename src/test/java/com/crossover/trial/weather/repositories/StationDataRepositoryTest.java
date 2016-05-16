package com.crossover.trial.weather.repositories;

import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.DataPointType;
import com.crossover.trial.weather.domain.measurement.ImmutableDataPoint;
import com.crossover.trial.weather.lib.TimestampFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StationDataRepositoryTest {

    @Mock
    private TimestampFactory timestampFactory;

    @InjectMocks
    private StationDataRepository repository;

    @Test
    public void shouldAcceptWindDataInValidRange() {
        repository.update(DataPointType.WIND, createData(0.0));

        assertThat(repository.toData().wind()).isEqualTo(createData(0.0));
    }

    @Test
    public void shouldIgnoreWindDataInValidRange() {
        repository.update(DataPointType.WIND, createData(-0.0001));

        assertThat(repository.toData().wind()).isNull();
    }

    @Test
    public void shouldAcceptTemperatureDataAtLowestValidValue() {
        final DataPoint data = createData(-50.0);
        repository.update(DataPointType.TEMPERATURE, data);

        assertThat(repository.toData().temperature()).isEqualTo(data);
    }

    @Test
    public void shouldAcceptTemperatureDataAtHighestValidValue() {
        final DataPoint data = createData(99.999999);
        repository.update(DataPointType.TEMPERATURE, data);

        assertThat(repository.toData().temperature()).isEqualTo(data);
    }

    @Test
    public void shouldIgnoreTemperatureDataBelowLowestValidValue() {
        repository.update(DataPointType.TEMPERATURE, createData(-50.0001));

        assertThat(repository.toData().temperature()).isNull();
    }

    @Test
    public void shouldIgnoreTemperatureDataAboveHighestValidValue() {
        repository.update(DataPointType.TEMPERATURE, createData(100.0));

        assertThat(repository.toData().temperature()).isNull();
    }

    private ImmutableDataPoint createData(double mean) {
        return ImmutableDataPoint.builder().first(0).median(1).last(0).count(1).mean(mean).build();
    }
}