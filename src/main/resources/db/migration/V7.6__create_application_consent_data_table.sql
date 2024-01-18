CREATE TABLE application_consent_data (
    id                 SERIAL PRIMARY KEY,
    application_id     INTEGER NOT NULL,
    consent_start_date DATE    NOT NULL,
    consent_end_date   DATE    NOT NULL,
    CONSTRAINT application_consent_data_fk FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT application_consent_data_application_id_unq UNIQUE (application_id)
);

CREATE INDEX application_consent_figures_application_id_idx
    ON application_consent_data(application_id);
