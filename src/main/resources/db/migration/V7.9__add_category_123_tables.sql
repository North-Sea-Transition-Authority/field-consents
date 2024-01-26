--
-- flare
--
CREATE TABLE IF NOT EXISTS flare_annual_123_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    category_1             NUMERIC NOT NULL,
    category_2             NUMERIC NOT NULL,
    category_3             NUMERIC NOT NULL,
    comments               TEXT,
    CONSTRAINT flare_annual_123_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_annual_123_months_idx1_av_id ON flare_annual_123_months(application_version_id);

CREATE TABLE IF NOT EXISTS flare_short_term_123_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    start_date             DATE NOT NULL,
    end_date               DATE NOT NULL,
    category_1             NUMERIC NOT NULL,
    category_2             NUMERIC NOT NULL,
    category_3             NUMERIC NOT NULL,
    comments               TEXT,
    CONSTRAINT flare_short_term_123_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_short_term_123_months_idx1_av_id ON flare_short_term_123_months(application_version_id);

CREATE TABLE IF NOT EXISTS flare_report_123_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    category_1             NUMERIC NOT NULL,
    category_2             NUMERIC NOT NULL,
    category_3             NUMERIC NOT NULL,
    shut_down_days         INTEGER NOT NULL,
    comments               TEXT,
    CONSTRAINT flare_report_123_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_report_123_months_idx1_av_id ON flare_report_123_months(application_version_id);

CREATE TABLE IF NOT EXISTS flare_report_123_gas_data (
    id                                  SERIAL PRIMARY KEY,
    application_version_id              INTEGER NOT NULL,
    category_1_density                  NUMERIC,
    category_1_inert_percentage         NUMERIC,
    category_1_hydro_percentage         NUMERIC,
    category_2_density                  NUMERIC,
    category_2_inert_percentage         NUMERIC,
    category_2_hydro_percentage         NUMERIC,
    category_3_density                  NUMERIC,
    category_3_inert_percentage         NUMERIC,
    category_3_hydro_percentage         NUMERIC,
    CONSTRAINT flare_report_123_gas_data_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_report_123_gas_data_idx1_av_id ON flare_report_123_gas_data(application_version_id);

--
-- vent
--
CREATE TABLE IF NOT EXISTS vent_annual_123_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    category_1             NUMERIC NOT NULL,
    comments               TEXT,
    CONSTRAINT vent_annual_123_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_annual_123_months_idx1_av_id ON vent_annual_123_months(application_version_id);

CREATE TABLE IF NOT EXISTS vent_short_term_123_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    start_date             DATE NOT NULL,
    end_date               DATE NOT NULL,
    category_1             NUMERIC NOT NULL,
    comments               TEXT,
    CONSTRAINT vent_short_term_123_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_short_term_123_months_idx1_av_id ON vent_short_term_123_months(application_version_id);

CREATE TABLE IF NOT EXISTS vent_report_123_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    category_1             NUMERIC NOT NULL,
    shut_down_days         INTEGER NOT NULL,
    comments               TEXT,
    CONSTRAINT vent_report_123_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_report_123_months_idx1_av_id ON vent_report_123_months(application_version_id);

CREATE TABLE IF NOT EXISTS vent_report_123_gas_data (
    id                                  SERIAL PRIMARY KEY,
    application_version_id              INTEGER NOT NULL,
    category_1_density                  NUMERIC,
    category_1_inert_percentage         NUMERIC,
    category_1_hydro_percentage         NUMERIC,
    CONSTRAINT vent_report_123_gas_data_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_report_123_gas_data_idx1_av_id ON vent_report_123_gas_data(application_version_id);
