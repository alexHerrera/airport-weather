package com.crossover.trial.weather.domain.measurement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

/**
 * encapsulates sensor information for a particular location
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAtmosphericData.class)
@JsonDeserialize(as = ImmutableAtmosphericData.class)
public interface AtmosphericData {

    /**
     * temperature in degrees celsius
     */
    @Nullable
    DataPoint temperature();

    /**
     * wind speed in km/h
     */
    @Nullable
    DataPoint wind();

    /**
     * humidity in percent
     */
    @Nullable
    DataPoint humidity();

    /**
     * precipitation in cm
     */
    @Nullable
    DataPoint precipitation();

    /**
     * pressure in mmHg
     */
    @Nullable
    DataPoint pressure();

    /**
     * cloud cover percent from 0 - 100 (integer)
     */
    @Nullable
    DataPoint cloudCover();

    /**
     * the last time this data was updated, in milliseconds since UTC epoch
     */
    long lastUpdateTime();
}
