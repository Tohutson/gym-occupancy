CREATE TABLE IF NOT EXISTS facility_counts (
    id BIGSERIAL PRIMARY KEY,
    facility_id VARCHAR(255),
    facility_name VARCHAR(255),
    location_name VARCHAR(255) NOT NULL,
    total_capacity INTEGER NOT NULL,
    last_count INTEGER NOT NULL,
    is_closed BOOLEAN NOT NULL,
    last_updated_date_and_time TIMESTAMPTZ NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE facility_counts ADD COLUMN IF NOT EXISTS facility_id VARCHAR(255);

UPDATE facility_counts
SET facility_id = location_name
WHERE facility_id IS NULL;

ALTER TABLE facility_counts ALTER COLUMN facility_id SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'facility_counts'
          AND column_name = 'last_updated_date_and_time'
          AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE facility_counts
            ALTER COLUMN last_updated_date_and_time TYPE TIMESTAMPTZ
            USING last_updated_date_and_time AT TIME ZONE 'America/New_York';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'facility_counts'
          AND column_name = 'recorded_at'
          AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE facility_counts
            ALTER COLUMN recorded_at TYPE TIMESTAMPTZ
            USING recorded_at AT TIME ZONE 'America/New_York';
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_facility_measurement
    ON facility_counts (facility_id, last_updated_date_and_time);

CREATE INDEX IF NOT EXISTS idx_facility_recorded_at
    ON facility_counts (facility_id, recorded_at);

CREATE INDEX IF NOT EXISTS idx_recorded_at
    ON facility_counts (recorded_at);
