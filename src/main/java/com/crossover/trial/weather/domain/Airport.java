package com.crossover.trial.weather.domain;

import com.crossover.trial.weather.domain.gis.Point;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Basic airport information.
 */

@Value.Immutable
@JsonSerialize(as = ImmutableAirport.class)
@JsonDeserialize(as = ImmutableAirport.class)
public interface Airport extends Point {

    @JsonProperty("iata")
    String iataCode();
}
