package com.crossover.trial.weather;

import com.crossover.trial.weather.lib.CsvSplitter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 * <p>
 * TODO: Implement the Airport Loader
 */
public class AirportLoader {

    private final CsvSplitter csvSplitter = new CsvSplitter();
    /**
     * end point to supply updates
     */
    private WebTarget collect;

    public AirportLoader(String baseUrl) {
        Client client = ClientBuilder.newClient();
        collect = client.target(baseUrl + "/collect");
    }

    public static void main(String args[]) throws IOException {
        File airportDataFile = new File(args[0]);
        if (!airportDataFile.exists() || airportDataFile.length() == 0) {
            System.err.println(airportDataFile + " is not a valid input");
            System.exit(1);
        }

        AirportLoader al = new AirportLoader("http://localhost:9090");
        al.upload(airportDataFile);
        System.exit(0);
    }

    public void upload(File airportDataFile) throws IOException {
        try (InputStream airportDataStream = new FileInputStream(airportDataFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream, "UTF-8"))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        }
    }

    private void processLine(String line) {
        String[] columns = csvSplitter.split(line);
        if (columns.length <= 7) {
            return;
        }

        String iataCode = extractIataCode(columns);
        String path = extractRequestPath(columns);

        Response post = collect.path(path).request().post(Entity.text(""));

        processResultStatus(iataCode, post);
    }

    private String extractIataCode(String[] columns) {
        return columns[4].replace("\"", "");
    }

    private String extractRequestPath(String[] columns) {
        String iataCode = extractIataCode(columns);
        String latitude = columns[6];
        String longitude = columns[7];

        return "/airport/" + iataCode + "/" + latitude + "/" + longitude;
    }

    private void processResultStatus(String iataCode, Response post) {
        switch (post.getStatus()) {
            case 200:
                break;

            case 403:
                System.out.println("Warning: airport entry '" + iataCode + "' already exists");
                break;

            default:
                System.out.println("ERROR when adding airport '" + iataCode + "': " + post.getStatus() + " " + post.getStatusInfo());
        }
    }
}
