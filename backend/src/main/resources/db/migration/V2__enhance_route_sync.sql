ALTER TABLE route
    ADD COLUMN source VARCHAR(200),
    ADD COLUMN destination VARCHAR(200),
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Existing data was previously stored as:
-- source → destination
UPDATE route
SET
    source = TRIM(SPLIT_PART(name, ' → ', 1)),
    destination = TRIM(SPLIT_PART(name, ' → ', 2))
WHERE source IS NULL
   OR destination IS NULL;

-- Known route-code normalization:
-- old application data used 134, current timetable source uses 134B.
UPDATE route
SET route_code = '134B',
           updated_at = CURRENT_TIMESTAMP
WHERE route_code = '134'
  AND NOT EXISTS (
      SELECT 1
      FROM route r2
      WHERE r2.agency_id = route.agency_id
        AND r2.route_code = '134B'
  );