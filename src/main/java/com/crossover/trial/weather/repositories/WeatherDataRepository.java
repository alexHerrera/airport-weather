package com.crossover.trial.weather.repositories;

import com.crossover.trial.weather.domain.measurement.AtmosphericData;
import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.DataPointType;
import javaslang.collection.Seq;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeatherDataRepository {

    private final ConcurrentHashMap<String, StationDataRepository> stationDataRepositoryByStationId = new ConcurrentHashMap<>();

    @Inject
    private Provider<StationDataRepository> stationDataRepositoryProvider;

    /**
     * Update data for station with stationId, creates a new {@link StationDataRepository} if {@code stationId} is not known.
     *
     * @param stationId id which is used to reference the station
     * @param dataType  type of data
     * @param data      data to be updated
     */
    public void update(String stationId, DataPointType dataType, DataPoint data) {
        stationDataRepositoryByStationId
                .computeIfAbsent(stationId, k -> stationDataRepositoryProvider.get())
                .update(dataType, data);
    }

    public Seq<AtmosphericData> getWeatherData() {
        return Stream.ofAll(stationDataRepositoryByStationId.values())
                .map(StationDataRepository::toData);
    }

    public Option<AtmosphericData> getWeatherDataFor(String stationId) {
        return Option.of(stationDataRepositoryByStationId.get(stationId))
                .map(StationDataRepository::toData);
    }

    public void removeStation(String stationId) {
        stationDataRepositoryByStationId.remove(stationId);
    }
}
