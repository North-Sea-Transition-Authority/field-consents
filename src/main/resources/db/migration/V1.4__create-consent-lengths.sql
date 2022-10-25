CREATE TABLE IF NOT EXISTS consent_lengths (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    consent_length          TEXT NOT NULL,
    annual_consent_year     INTEGER,
    short_term_start_date   DATE,
    short_term_end_date     DATE,
    long_term_start_year    INTEGER,
    long_term_end_year      INTEGER,
    CONSTRAINT app_types_fk1_version_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX app_types_idx1_version_id ON consent_lengths(application_version_id);