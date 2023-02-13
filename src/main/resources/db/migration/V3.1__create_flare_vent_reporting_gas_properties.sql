CREATE TABLE IF NOT EXISTS flare_report_gas_data (
    id                                  SERIAL PRIMARY KEY,
    application_version_id              INTEGER NOT NULL,
    category_a_density                  NUMERIC NOT NULL,
    category_a_inert_percentage         NUMERIC NOT NULL,
    category_a_hydro_percentage         NUMERIC NOT NULL,
    category_b_density                  NUMERIC NOT NULL,
    category_b_inert_percentage         NUMERIC NOT NULL,
    category_b_hydro_percentage         NUMERIC NOT NULL,
    category_c_density                  NUMERIC NOT NULL,
    category_c_inert_percentage         NUMERIC NOT NULL,
    category_c_hydro_percentage         NUMERIC NOT NULL,
    evaluated_per_category              BOOLEAN NOT NULL,
    evaluated_per_category_explanation  TEXT,
    CONSTRAINT flare_report_gas_data_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_report_gas_data_idx1_av_id ON flare_report_gas_data(application_version_id);

CREATE TABLE IF NOT EXISTS vent_report_gas_data (
    id                                  SERIAL PRIMARY KEY,
    application_version_id              INTEGER NOT NULL,
    category_a_density                  NUMERIC NOT NULL,
    category_a_inert_percentage         NUMERIC NOT NULL,
    category_a_hydro_percentage         NUMERIC NOT NULL,
    category_b_density                  NUMERIC NOT NULL,
    category_b_inert_percentage         NUMERIC NOT NULL,
    category_b_hydro_percentage         NUMERIC NOT NULL,
    category_c_density                  NUMERIC NOT NULL,
    category_c_inert_percentage         NUMERIC NOT NULL,
    category_c_hydro_percentage         NUMERIC NOT NULL,
    evaluated_per_category              BOOLEAN NOT NULL,
    evaluated_per_category_explanation  TEXT,
    CONSTRAINT vent_report_gas_data_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_report_gas_data_idx1_av_id ON vent_report_gas_data(application_version_id);

ALTER TABLE application_units
ADD COLUMN flare_gas_density_unit TEXT,
ADD COLUMN flare_gas_content_unit TEXT,
ADD COLUMN vent_gas_density_unit TEXT,
ADD COLUMN vent_gas_content_unit TEXT;