
--DROP SEQUENCE fcs_migration.consent_length_id_seq;
--DROP TABLE fcs_migration.consent_lengths;
--DROP SEQUENCE fcs_migration.application_id_seq;
--DROP TABLE fcs_migration.application_versions;
--DROP TABLE fcs_migration.applications;

---- 2041
--SELECT max(id)
--FROM envmgr.field_consents fc
--/

--
-- applications
--

CREATE SEQUENCE fcs_migration.application_id_seq;

CREATE TABLE fcs_migration.applications (
  id                INTEGER PRIMARY KEY
, type              VARCHAR2(4000) NOT NULL
, created_date      DATE
, created_by_wua_id INTEGER NOT NULL
, variation_no      INTEGER NOT NULL
, application_no    INTEGER NOT NULL
, fc_id             INTEGER NOT NULL
);


--
-- application_versions
--

CREATE TABLE fcs_migration.application_versions (
  id                           INTEGER PRIMARY KEY
, application_id               INTEGER NOT NULL
                               CONSTRAINT app_versions_fk1_app_id
                               REFERENCES fcs_migration.applications
, version_no                   INTEGER NOT NULL
, primary_operator_ou_id       INTEGER NOT NULL
, cached_primary_operator_name VARCHAR2(4000)
, status                       VARCHAR2(4000) NOT NULL
, created_date_time            DATE NOT NULL
, created_by_wua_id            INTEGER NOT NULL
, submitted_date_time          DATE
, submitted_by_wua_id          INTEGER
, case_officer_wua_id          INTEGER
);

--
-- consent_lengths
--

CREATE SEQUENCE fcs_migration.consent_length_id_seq;

CREATE TABLE fcs_migration.consent_lengths (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT app_types_fk1_version_id
                         REFERENCES application_versions
, consent_length         VARCHAR2(4000) NOT NULL
, annual_consent_year    INTEGER
, short_term_start_date  DATE
, short_term_end_date    DATE
, long_term_start_year   INTEGER
, long_term_end_year     INTEGER
);
