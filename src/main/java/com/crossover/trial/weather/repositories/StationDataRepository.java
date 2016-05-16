package com.crossover.trial.weather.repositories;

import com.crossover.trial.weather.domain.measurement.AtmosphericData;
import com.crossover.trial.weather.domain.measurement.DataPoint;
import com.crossover.trial.weather.domain.measurement.DataPointType;
import com.crossover.trial.weather.domain.measurement.ImmutableAtmosphericData;
import com.crossover.trial.weather.lib.TimestampFactory;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Option;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class StationDataRepository {
    private static final Map<DataPointType, Predicate<DataPoint>> acceptanceRuleByType = HashMap.ofEntries(
            Tuple.of(DataPointType.WIND, (Predicate<DataPoint>) dataPoint -> dataPoint.mean() >= 0.0),
            Tuple.of(DataPointType.TEMPERATURE, (Predicate<DataPoint>) dataPoint -> dataPoint.mean() >= -50.0 && dataPoint.mean() < 100.0),
            Tuple.of(DataPointType.HUMIDITY, (Predicate<DataPoint>) dataPoint -> dataPoint.mean() >= 0.0 && dataPoint.mean() < 100.0),
            Tuple.of(DataPointType.PRESSURE, (Predicate<DataPoint>) dataPoint -> dataPoint.mean() >= 650.0 && dataPoint.mean() < 800.0),
            Tuple.of(DataPointType.CLOUDCOVER, (Predicate<DataPoint>) dataPoint -> dataPoint.mean() >= 0 && dataPoint.mean() < 100.0),
            Tuple.of(DataPointType.PRECIPITATION, (Predicate<DataPoint>) dataPoint -> dataPoint.mean() >= 0 && dataPoint.mean() < 100.0)
    );

    private static final List<Tuple2<DataPointType, BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>>> typesWithBuilderMethods = List.of(
            Tuple.of(DataPointType.WIND, (BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>) ImmutableAtmosphericData.Builder::wind),
            Tuple.of(DataPointType.TEMPERATURE, (BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>) ImmutableAtmosphericData.Builder::temperature),
            Tuple.of(DataPointType.HUMIDITY, (BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>) ImmutableAtmosphericData.Builder::humidity),
            Tuple.of(DataPointType.PRESSURE, (BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>) ImmutableAtmosphericData.Builder::pressure),
            Tuple.of(DataPointType.CLOUDCOVER, (BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>) ImmutableAtmosphericData.Builder::cloudCover),
            Tuple.of(DataPointType.PRECIPITATION, (BiConsumer<ImmutableAtmosphericData.Builder, DataPoint>) ImmutableAtmosphericData.Builder::precipitation)
    );

    private final ConcurrentHashMap<DataPointType, DataPoint> dataPointByType = new ConcurrentHashMap<>();

    private long lastUpdateTime = 0L;

    @Inject
    private TimestampFactory timestampFactory;

    void update(DataPointType dataType, DataPoint data) {
        final Boolean shouldAddData =
                acceptanceRuleByType
                        .get(dataType)
                        .map(rulePredicate -> rulePredicate.test(data))
                        .getOrElse(true);

        if (shouldAddData) {
            dataPointByType.put(dataType, data);
            lastUpdateTime = timestampFactory.getCurrentTimestamp();
        }
    }

    public AtmosphericData toData() {
        final ImmutableAtmosphericData.Builder builder = ImmutableAtmosphericData.builder();

        typesWithBuilderMethods.forEach(dataTypeAndBuilderMethod -> {
            DataPointType dataType = dataTypeAndBuilderMethod._1();
            BiConsumer<ImmutableAtmosphericData.Builder, DataPoint> builderAdapter = dataTypeAndBuilderMethod._2();

            Option.of(dataPointByType
                    .get(dataType))
                    .forEach(data -> {
                        builderAdapter.accept(builder, data);
                    });
        });

        builder.lastUpdateTime(lastUpdateTime);

        return builder.build();
    }
}
