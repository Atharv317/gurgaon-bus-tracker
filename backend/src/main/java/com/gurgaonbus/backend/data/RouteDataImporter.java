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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@Component
public class RouteDataImporter implements ApplicationRunner {

    private static final String DATA_FILE = "data/routes.csv";
    private static final String AGENCY_NAME = "Gurugram Metropolitan City Bus Limited";
    private static final String AGENCY_CODE = "GMCBL";

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

        try (Reader reader = openDataFile()) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader);

            int imported = 0;
            int skipped = 0;

            for (CSVRecord record : records) {
                String routeCode = record.get("route_code").trim();

                if (routeRepository
                        .findByAgencyIdAndRouteCode(agency.getId(), routeCode)
                        .isPresent()) {
                    skipped++;
                    continue;
                }

                Route route = new Route(
                        agency.getId(),
                        routeCode,
                        record.get("source").trim()
                                + " → "
                                + record.get("destination").trim(),
                        null,
                        OffsetDateTime.now()
                );

                routeRepository.save(route);
                imported++;
            }

            System.out.println(
                    "Route import completed. Imported: "
                            + imported
                            + ", Skipped: "
                            + skipped
            );
        }
    }

    private Agency getOrCreateAgency() {
        return agencyRepository.findByCode(AGENCY_CODE)
                .orElseGet(() -> agencyRepository.save(
                        new Agency(
                                AGENCY_NAME,
                                AGENCY_CODE,
                                OffsetDateTime.now()
                        )
                ));
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
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );
    }
}