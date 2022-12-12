CREATE TABLE IF NOT EXISTS vent_annual_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    category_a             NUMERIC NOT NULL,
    category_b             NUMERIC NOT NULL,
    category_c             NUMERIC NOT NULL,
    comments               TEXT,
    CONSTRAINT vent_annual_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_annual_months_idx1_av_id ON vent_annual_months(application_version_id);