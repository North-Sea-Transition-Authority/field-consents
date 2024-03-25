CREATE TABLE application_consents (
  id SERIAL NOT NULL
, application_id INTEGER NOT NULL
, issued_by_wua_id INTEGER NOT NULL
, issued_timestamp TIMESTAMPTZ NOT NULL
, CONSTRAINT application_consents_pk PRIMARY KEY (id)
, CONSTRAINT application_consents_application_id_fk FOREIGN KEY (application_id) REFERENCES applications (id)
, CONSTRAINT application_consents_application_id_unq UNIQUE (application_id)
);

CREATE INDEX application_consents_application_id_idx ON application_consents (application_id);
