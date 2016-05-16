package com.crossover.trial.weather.domain.measurement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * A collected point, including some information about the range of collected values
 */

@Gson.TypeAdapters
@Value.Immutable
@JsonSerialize(as = ImmutableDataPoint.class)
@JsonDeserialize(as = ImmutableDataPoint.class)
public interface DataPoint {

    /**
     * the mean of the observations
     */
    double mean();

    /**
     * 1st quartile -- useful as a lower bound
     */
    int first();

    /**
     * 2nd quartile -- median value
     */
    int median();

    /**
     * 3rd quartile value -- less noisy upper value
     */
    int last();

    /**
     * the total number of measurements
     */
    int count();
}
