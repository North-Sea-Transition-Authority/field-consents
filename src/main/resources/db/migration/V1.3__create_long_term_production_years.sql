CREATE TABLE IF NOT EXISTS long_term_production_years (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    oil_min_unit           TEXT NOT NULL,
    oil_min_value          NUMERIC NOT NULL,
    oil_max_unit           TEXT NOT NULL,
    oil_max_value          NUMERIC NOT NULL,
    gas_min_unit           TEXT NOT NULL,
    gas_min_value          NUMERIC NOT NULL,
    gas_max_unit           TEXT NOT NULL,
    gas_max_value          NUMERIC NOT NULL,
    CONSTRAINT long_term_prod_fk1_av_id
    FOREIGN KEY (application_version_id)
        REFERENCES application_versions (id)
);

CREATE INDEX long_term_prod_idx1_av_id ON long_term_production_years(application_version_id);