package com.gurgaonbus.backend.data;

import com.gurgaonbus.backend.agency.Agency;
import com.gurgaonbus.backend.agency.AgencyRepository;
import com.gurgaonbus.backend.route.Route;
import com.gurgaonbus.backend.route.RouteRepository;
import com.gurgaonbus.backend.routestop.RouteStop;
import com.gurgaonbus.backend.routestop.RouteStopRepository;
import com.gurgaonbus.backend.stop.Stop;
import com.gurgaonbus.backend.stop.StopRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class RouteStopDataImporter implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(RouteStopDataImporter.class);

    private static final String DATA_FILE = "data/route_stops.csv";
    private static final String AGENCY_CODE = "GMCBL";

    private final AgencyRepository agencyRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;

    public RouteStopDataImporter(
            AgencyRepository agencyRepository,
            RouteRepository routeRepository,
            StopRepository stopRepository,
            RouteStopRepository routeStopRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.routeStopRepository = routeStopRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        Agency agency = agencyRepository
                .findByCode(AGENCY_CODE)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Agency not found for code: " + AGENCY_CODE
                        )
                );

        Set<String> sourceRelationships = new HashSet<>();

        int inserted = 0;
        int skipped = 0;

        try (Reader reader = openDataFile()) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader);

            for (CSVRecord record : records) {

                String routeCode = required(
                        record.get("route_code"),
                        "route_code"
                );

                String direction = required(
                        record.get("direction"),
                        "direction"
                );

                int stopSequence = requiredPositiveInteger(
                        record.get("stop_sequence"),
                        "stop_sequence"
                );

                String stopCode = required(
                        record.get("stop_code"),
                        "stop_code"
                );

                String stopName = required(
                        record.get("stop_name"),
                        "stop_name"
                );

                String relationshipKey =
                        routeCode + "|" + direction + "|" + stopSequence;

                if (!sourceRelationships.add(relationshipKey)) {
                    throw new IllegalStateException(
                            "Duplicate route-stop sequence in "
                                    + DATA_FILE
                                    + ": "
                                    + relationshipKey
                    );
                }

                Route route = routeRepository
                        .findByAgencyIdAndRouteCode(
                                agency.getId(),
                                routeCode
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Route not found for route_code: "
                                                + routeCode
                                )
                        );

                Stop stop = stopRepository
                        .findByStopCode(stopCode)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Stop not found for stop_code: "
                                                + stopCode
                                )
                        );

                validateStopName(
                        stop,
                        stopName,
                        stopCode
                );

                if (routeStopRepository
                        .existsByRouteIdAndDirectionAndStopSequence(
                                route.getId(),
                                direction,
                                stopSequence
                        )) {

                    skipped++;
                    continue;
                }

                RouteStop routeStop = new RouteStop(
                        route.getId(),
                        stop.getId(),
                        stopSequence,
                        direction
                );

                routeStopRepository.save(routeStop);
                inserted++;
            }
        }

        log.info(
                "Route-stop synchronization completed. inserted={}, skipped={}",
                inserted,
                skipped
        );
    }

    private Reader openDataFile() {

        var inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(DATA_FILE);

        if (inputStream == null) {
            throw new IllegalStateException(
                    "Route-stop data file not found: " + DATA_FILE
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
            String field
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Required field is missing: " + field
            );
        }

        return value.trim();
    }

    private int requiredPositiveInteger(
            String value,
            String field
    ) {

        String normalized = required(value, field);

        try {

            int number = Integer.parseInt(normalized);

            if (number <= 0) {
                throw new IllegalArgumentException(
                        field + " must be greater than zero: " + number
                );
            }

            return number;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    field + " must be a valid integer: " + value,
                    exception
            );
        }
    }

    private void validateStopName(
            Stop stop,
            String sourceStopName,
            String stopCode
    ) {

        if (!stop.getName().equals(sourceStopName)) {

            log.warn(
                    "Stop name mismatch for stop_code={}. database='{}', source='{}'",
                    stopCode,
                    stop.getName(),
                    sourceStopName
            );
        }
    }
}