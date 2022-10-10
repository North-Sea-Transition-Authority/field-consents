CREATE TABLE IF NOT EXISTS short_term_production_months (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    year                    INTEGER NOT NULL,
    month                   TEXT NOT NULL,
    start_date              TIMESTAMP NOT NULL,
    end_date                TIMESTAMP NOT NULL,
    oil_min_unit            TEXT NOT NULL,
    oil_min_value           NUMERIC NOT NULL,
    oil_max_unit            TEXT NOT NULL,
    oil_max_value           NUMERIC NOT NULL,
    gas_min_unit            TEXT NOT NULL,
    gas_min_value           NUMERIC NOT NULL,
    gas_max_unit            TEXT NOT NULL,
    gas_max_value           NUMERIC NOT NULL,
    CONSTRAINT short_prod_fk1_version_id
    FOREIGN KEY (application_version_id)
        REFERENCES application_versions (id)
);

CREATE INDEX short_prod_idx1_version_id ON short_term_production_months(application_version_id);