CREATE TABLE IF NOT EXISTS application_other_legacy_data (
    id                            SERIAL PRIMARY KEY,
    application_version_id        INTEGER NOT NULL,
    increase_in_production        BOOLEAN,
    es_reference                  TEXT,
    uplift_percentage             INTEGER,
    field_location                TEXT,
    previous_year_consent_history NUMERIC,
    previous_year_actuals         NUMERIC,
    terminal_name                 TEXT,
    terminal_location             TEXT,
    project_under_eia_regs        BOOLEAN,
    UNIQUE (application_version_id),
    CONSTRAINT application_other_legacy_data_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX application_other_legacy_data_idx1_av_id ON application_other_legacy_data(application_version_id);
