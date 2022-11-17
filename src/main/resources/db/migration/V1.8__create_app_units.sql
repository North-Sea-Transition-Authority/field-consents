CREATE TABLE IF NOT EXISTS application_units (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    flare_category_unit    TEXT,
    vent_category_unit     TEXT,
    production_oil_unit    TEXT,
    production_gas_unit    TEXT,
    CONSTRAINT application_units_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX application_units_idx1_av_id ON application_units(application_version_id);