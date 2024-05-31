CREATE TABLE application_consent_data_long_term_emission_figures (
  id SERIAL
, application_id INTEGER NOT NULL
, year INTEGER NOT NULL
, daily_average NUMERIC NOT NULL
, CONSTRAINT application_consent_data_lt_emission_figures_id_pk PRIMARY KEY (id)
, CONSTRAINT application_consent_data_lt_emission_figures_app_id_fk FOREIGN KEY (application_id) REFERENCES applications (id)
, CONSTRAINT application_consent_data_lt_emission_figures_app_id_year_unq UNIQUE (application_id, year)
);

CREATE INDEX application_consent_data_lt_emission_figures_id_idx
    ON application_consent_data_long_term_emission_figures(application_id);

CREATE TABLE application_consent_data_long_term_emission_figures_aud (
  rev SERIAL
, revtype NUMERIC
, id SERIAL
, application_id INTEGER
, year INTEGER
, daily_average NUMERIC
, PRIMARY KEY (rev, id)
, FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX application_consent_data_lt_emission_figures_aud_rev_idx
    ON application_consent_data_long_term_emission_figures_aud(rev);
