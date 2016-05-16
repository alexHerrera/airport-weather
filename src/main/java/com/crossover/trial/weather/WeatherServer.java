package com.crossover.trial.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationListener;


/**
 * This main method will be use by the automated functional grader. You shouldn't move this class or remove the
 * main method. You may change the implementation, but we encourage caution.
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "com.crossover.trial.weather")
public class WeatherServer extends SpringBootServletInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port}")
    private String serverPort;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WeatherServer.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        // the autograder waits for this output before running automated tests, please don't remove it
        System.out.println("\nStarting Weather App local testing server: http://localhost:" + serverPort + "\n");
    }
}
