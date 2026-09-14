package com.gurgaonbus.backend.data;

import com.gurgaonbus.backend.stop.Stop;
import com.gurgaonbus.backend.stop.StopRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StopDataImporter implements ApplicationRunner {

    private static final String DATA_FILE = "data/stops.csv";

    private static final int SRID = 4326;

    private static final Logger log =
            LoggerFactory.getLogger(RouteDataImporter.class);

    private final GeometryFactory geometryFactory =
            new GeometryFactory(
                    new PrecisionModel(),
                    SRID
            );

    private final StopRepository stopRepository;

    public StopDataImporter(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        Set<String> sourceStopCodes = new HashSet<>();

        int inserted = 0;
        int updated = 0;

        try (Reader reader = openDataFile()) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader);

            for (CSVRecord record : records) {

                String stopCode = requiredWithMaxLength(
                        record.get("stop_code"),
                        "stop_code",
                        100
                );

                String name = requiredWithMaxLength(
                        record.get("name"),
                        "name",
                        200
                );

                double latitude = requiredDouble(
                        record.get("latitude"),
                        "latitude"
                );

                double longitude = requiredDouble(
                        record.get("longitude"),
                        "longitude"
                );

                validateCoordinates(
                        latitude,
                        longitude,
                        stopCode
                );

                if (!sourceStopCodes.add(stopCode)) {
                    throw new IllegalStateException(
                            "Duplicate stop_code in stops.csv: "
                                    + stopCode
                    );
                }

                var existingStop =
                        stopRepository.findByStopCode(stopCode);

                Point location = geometryFactory.createPoint(
                        new Coordinate(longitude, latitude)
                );

                if (existingStop.isPresent()) {

                    Stop stop = existingStop.get();

                    stop.updateFromSource(
                            name,
                            latitude,
                            longitude,
                            location
                    );

                    updated++;

                } else {

                    Stop stop = new Stop(
                            stopCode,
                            name,
                            latitude,
                            longitude,
                            OffsetDateTime.now(),
                            location
                    );

                    stopRepository.save(stop);

                    inserted++;
                }
            }
        }

        log.info(
                "Stop synchronization completed. inserted={}, updated={}",
                inserted,
                updated
        );
    }

    private Reader openDataFile() {

        var inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(DATA_FILE);

        if (inputStream == null) {
            throw new IllegalStateException(
                    "Stop data file not found: " + DATA_FILE
            );
        }

        return new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        );
    }

    private String required(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Invalid stops.csv: "
                            + fieldName
                            + " cannot be blank"
            );
        }

        return value.trim();
    }

    private double requiredDouble(
            String value,
            String fieldName
    ) {

        String normalized = required(value, fieldName);

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid stops.csv: "
                            + fieldName
                            + " must be a number: "
                            + value,
                    exception
            );
        }
    }

    private void validateCoordinates(
            double latitude,
            double longitude,
            String stopCode
    ) {

        if (!Double.isFinite(latitude)
                || latitude < -90
                || latitude > 90) {

            throw new IllegalStateException(
                    "Invalid latitude for stop: "
                            + stopCode
            );
        }

        if (!Double.isFinite(longitude)
                || longitude < -180
                || longitude > 180) {

            throw new IllegalStateException(
                    "Invalid longitude for stop: "
                            + stopCode
            );
        }
    }

    private String requiredWithMaxLength(
            String value,
            String fieldName,
            int maxLength
    ) {
        String normalized = required(value, fieldName);

        if (normalized.length() > maxLength) {
            throw new IllegalStateException(
                    "Invalid stops.csv: "
                            + fieldName
                            + " exceeds maximum length of "
                            + maxLength
                            + ": "
                            + normalized
            );
        }

        return normalized;
    }
}