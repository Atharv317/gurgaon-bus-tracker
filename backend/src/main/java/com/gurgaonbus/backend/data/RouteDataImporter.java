package com.gurgaonbus.backend.data;

import com.gurgaonbus.backend.agency.Agency;
import com.gurgaonbus.backend.agency.AgencyRepository;
import com.gurgaonbus.backend.route.Route;
import com.gurgaonbus.backend.route.RouteRepository;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RouteDataImporter implements ApplicationRunner {

    private static final String DATA_FILE = "data/routes.csv";

    private static final String AGENCY_NAME =
            "Gurugram Metropolitan City Bus Limited";

    private static final String AGENCY_CODE = "GMCBL";

    private static final Logger log =
            LoggerFactory.getLogger(StopDataImporter.class);

    private final AgencyRepository agencyRepository;
    private final RouteRepository routeRepository;

    public RouteDataImporter(
            AgencyRepository agencyRepository,
            RouteRepository routeRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        Agency agency = getOrCreateAgency();

        Set<String> sourceRouteCodes = new HashSet<>();

        int inserted = 0;
        int updated = 0;
        int deactivated = 0;

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

                String source = required(
                        record.get("source"),
                        "source"
                );

                String destination = required(
                        record.get("destination"),
                        "destination"
                );

                sourceRouteCodes.add(routeCode);

                var existingRoute =
                        routeRepository.findByAgencyIdAndRouteCode(
                                agency.getId(),
                                routeCode
                        );

                if (existingRoute.isPresent()) {

                    Route route = existingRoute.get();

                    route.updateFromSource(
                            source,
                            destination,
                            null
                    );

                    routeRepository.save(route);

                    updated++;

                } else {

                    Route route = new Route(
                            agency.getId(),
                            routeCode,
                            source,
                            destination,
                            null,
                            OffsetDateTime.now()
                    );

                    routeRepository.save(route);

                    inserted++;
                }
            }
        }

        /*
         * Anything currently active in DB but absent from
         * the source CSV is considered inactive.
         */
        for (Route route : routeRepository.findAllByAgencyId(
                agency.getId()
        )) {

            if (route.isActive()
                    && !sourceRouteCodes.contains(route.getRouteCode())) {

                route.deactivate();
                routeRepository.save(route);

                deactivated++;
            }
        }

        log.info(
                "Route synchronization completed. inserted={}, updated={}, deactivated={}",
                inserted,
                updated,
                deactivated
        );
    }

    private Agency getOrCreateAgency() {

        return agencyRepository.findByCode(AGENCY_CODE)
                .orElseGet(() ->
                        agencyRepository.save(
                                new Agency(
                                        AGENCY_NAME,
                                        AGENCY_CODE,
                                        OffsetDateTime.now()
                                )
                        )
                );
    }

    private Reader openDataFile() {

        var inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(DATA_FILE);

        if (inputStream == null) {
            throw new IllegalStateException(
                    "Route data file not found: " + DATA_FILE
            );
        }

        return new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        );
    }

    private String required(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Invalid routes.csv: " + fieldName
                            + " cannot be blank"
            );
        }

        return value.trim();
    }
}