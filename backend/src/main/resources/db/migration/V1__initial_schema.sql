CREATE EXTENSION IF NOT EXISTS postgis;

-- =========================================================
-- AGENCY
-- =========================================================

CREATE TABLE agency (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- ROUTE
-- =========================================================

CREATE TABLE route (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL,
    route_code VARCHAR(50) NOT NULL,
    name VARCHAR(150),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_route_agency
        FOREIGN KEY (agency_id)
        REFERENCES agency(id),

    CONSTRAINT uq_route_agency_code
        UNIQUE (agency_id, route_code)
);

CREATE INDEX idx_route_agency_id
    ON route(agency_id);

-- =========================================================
-- STOP
-- =========================================================

CREATE TABLE stop (
    id BIGSERIAL PRIMARY KEY,
    stop_code VARCHAR(50) UNIQUE,
    name VARCHAR(200) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stop_location
    ON stop
    USING GIST (location);

-- =========================================================
-- ROUTE STOP
-- =========================================================

CREATE TABLE route_stop (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    stop_id BIGINT NOT NULL,
    stop_sequence INTEGER NOT NULL,
    direction VARCHAR(50),

    CONSTRAINT fk_route_stop_route
        FOREIGN KEY (route_id)
        REFERENCES route(id),

    CONSTRAINT fk_route_stop_stop
        FOREIGN KEY (stop_id)
        REFERENCES stop(id),

    CONSTRAINT uq_route_stop_sequence
        UNIQUE (route_id, direction, stop_sequence),

    CONSTRAINT uq_route_stop
        UNIQUE (route_id, direction, stop_id)
);

CREATE INDEX idx_route_stop_route_id
    ON route_stop(route_id);

CREATE INDEX idx_route_stop_stop_id
    ON route_stop(stop_id);

-- =========================================================
-- VEHICLE
-- =========================================================

CREATE TABLE vehicle (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL,
    vehicle_number VARCHAR(100) NOT NULL,
    registration_number VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicle_agency
        FOREIGN KEY (agency_id)
        REFERENCES agency(id),

    CONSTRAINT uq_vehicle_agency_number
        UNIQUE (agency_id, vehicle_number)
);

CREATE INDEX idx_vehicle_agency_id
    ON vehicle(agency_id);

-- =========================================================
-- TRIP
-- =========================================================

CREATE TABLE trip (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    vehicle_id BIGINT,
    service_date DATE NOT NULL,
    direction VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trip_route
        FOREIGN KEY (route_id)
        REFERENCES route(id),

    CONSTRAINT fk_trip_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicle(id)
);

CREATE INDEX idx_trip_route_service_date
    ON trip(route_id, service_date);

CREATE INDEX idx_trip_vehicle_id
    ON trip(vehicle_id);

CREATE INDEX idx_trip_status
    ON trip(status);

-- =========================================================
-- SCHEDULED STOP TIME
-- =========================================================

CREATE TABLE scheduled_stop_time (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    stop_id BIGINT NOT NULL,
    stop_sequence INTEGER NOT NULL,
    scheduled_arrival_time TIME NOT NULL,
    scheduled_departure_time TIME,

    CONSTRAINT fk_scheduled_stop_time_trip
        FOREIGN KEY (trip_id)
        REFERENCES trip(id),

    CONSTRAINT fk_scheduled_stop_time_stop
        FOREIGN KEY (stop_id)
        REFERENCES stop(id),

    CONSTRAINT uq_scheduled_stop_time
        UNIQUE (trip_id, stop_sequence),

    CONSTRAINT uq_scheduled_stop_time_stop
        UNIQUE (trip_id, stop_id)
);

CREATE INDEX idx_scheduled_stop_time_trip_id
    ON scheduled_stop_time(trip_id);

CREATE INDEX idx_scheduled_stop_time_stop_id
    ON scheduled_stop_time(stop_id);