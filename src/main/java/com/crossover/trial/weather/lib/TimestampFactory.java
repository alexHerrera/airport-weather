package com.crossover.trial.weather.lib;

import org.springframework.stereotype.Component;

@Component
public class TimestampFactory {
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
