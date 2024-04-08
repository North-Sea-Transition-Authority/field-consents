CREATE TABLE application_consent_field_equity_partners (
  id SERIAL NOT NULL
, application_consent_id INTEGER NOT NULL
, organisation_unit_id INTEGER NOT NULL
, organisation_name TEXT NOT NULL
, registered_number TEXT
, CONSTRAINT application_cfep_pk PRIMARY KEY (id)
, CONSTRAINT application_cfep_consent_id_org_unit_id_unq UNIQUE (application_consent_id, organisation_unit_id)
, CONSTRAINT application_cfep_consent_id_fk FOREIGN KEY (application_consent_id) REFERENCES application_consents (id)
);

CREATE INDEX application_cfep_consent_id_idx ON application_consent_field_equity_partners (application_consent_id);
