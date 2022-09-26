CREATE TABLE IF NOT EXISTS annual_production_months (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    year                    INTEGER NOT NULL,
    month                   TEXT NOT NULL,
    oil_min_unit            TEXT NOT NULL,
    oil_min_value           NUMERIC NOT NULL,
    oil_max_unit            TEXT NOT NULL,
    oil_max_value           NUMERIC NOT NULL,
    gas_min_unit            TEXT NOT NULL,
    gas_min_value           NUMERIC NOT NULL,
    gas_max_unit            TEXT NOT NULL,
    gas_max_value           NUMERIC NOT NULL,
    CONSTRAINT annual_prod_fk1_version_id
    FOREIGN KEY (application_version_id)
        REFERENCES application_versions (id)
);

CREATE INDEX annual_prod_idx1_version_id ON annual_production_months(application_version_id);