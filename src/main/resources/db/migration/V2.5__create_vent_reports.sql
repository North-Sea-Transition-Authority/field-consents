CREATE TABLE IF NOT EXISTS vent_report_months (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    month                  TEXT NOT NULL,
    category_a             NUMERIC NOT NULL,
    category_b             NUMERIC NOT NULL,
    category_c             NUMERIC NOT NULL,
    shut_down_days         INTEGER NOT NULL,
    comments               TEXT,
    CONSTRAINT vent_report_months_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_report_months_idx1_av_id ON vent_report_months(application_version_id);

CREATE TABLE IF NOT EXISTS vent_report_periods (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    report_end_month       TEXT NOT NULL,
    report_end_year        INTEGER NOT NULL,
    CONSTRAINT vent_report_periods_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_report_periods_idx1_av_id ON vent_report_periods(application_version_id);