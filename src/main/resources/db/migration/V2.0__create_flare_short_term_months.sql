CREATE TABLE IF NOT EXISTS flare_short_term_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    start_date             TIMESTAMP NOT NULL,
    end_date               TIMESTAMP NOT NULL,
    category_a             NUMERIC NOT NULL,
    category_b             NUMERIC NOT NULL,
    category_c             NUMERIC NOT NULL,
    comments               TEXT,
    CONSTRAINT flare_short_term_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_short_term_months_idx1_av_id ON flare_short_term_months(application_version_id);