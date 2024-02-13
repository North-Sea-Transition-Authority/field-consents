ALTER TABLE application_consent_data
  ADD COLUMN short_term_or_annual_production_min_oil NUMERIC
, ADD COLUMN short_term_or_annual_production_max_oil NUMERIC
, ADD COLUMN short_term_or_annual_production_min_gas NUMERIC
, ADD COLUMN short_term_or_annual_production_max_gas NUMERIC
, ADD COLUMN emission_daily_average NUMERIC;

CREATE TABLE application_consent_production_long_term_figures (
  id SERIAL
, application_id INTEGER NOT NULL
, year INTEGER NOT NULL
, min_oil NUMERIC NOT NULL
, max_oil NUMERIC NOT NULL
, min_gas NUMERIC NOT NULL
, max_gas NUMERIC NOT NULL
, CONSTRAINT application_consent_prod_long_term_figures_pk PRIMARY KEY (id)
, CONSTRAINT application_consent_prod_long_term_figures_app_id_fk FOREIGN KEY (application_id) REFERENCES applications (id)
, CONSTRAINT application_consent_prod_long_term_figures_app_id_year_unq UNIQUE (application_id, year) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX application_consent_prod_long_term_figures_app_id_idx ON application_consent_production_long_term_figures (application_id);
