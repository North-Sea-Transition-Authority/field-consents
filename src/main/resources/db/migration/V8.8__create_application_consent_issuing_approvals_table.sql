CREATE TABLE application_consent_issuing_approvals (
  id SERIAL NOT NULL
, application_id INTEGER NOT NULL
, approved_by_wua_id INTEGER NOT NULL
, approved_timestamp TIMESTAMPTZ NOT NULL
, CONSTRAINT application_consent_issuing_approvals_pk PRIMARY KEY (id)
, CONSTRAINT application_consent_issuing_approvals_application_id_fk FOREIGN KEY (application_id) REFERENCES applications (id)
, CONSTRAINT application_consent_issuing_approvals_application_id_unq UNIQUE (application_id)
);

CREATE INDEX application_consent_issuing_approvals_application_id_idx ON application_consent_issuing_approvals (application_id);

CREATE TABLE application_consent_issuing_approvals_aud (
  rev SERIAL
, revtype NUMERIC
, id INTEGER
, application_id INTEGER
, approved_by_wua_id INTEGER
, approved_timestamp TIMESTAMPTZ
, CONSTRAINT application_consent_issuing_approvals_aud_pk PRIMARY KEY (rev, id)
, CONSTRAINT application_consent_issuing_approvals_aud_rev_fk FOREIGN KEY (rev) REFERENCES audit_revisions (rev)
);

CREATE INDEX application_consent_issuing_approvals_aud_rev_idx ON application_consent_issuing_approvals_aud (rev);
