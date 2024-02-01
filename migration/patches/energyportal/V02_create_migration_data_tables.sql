
--DROP TABLE fcs_migration.file_upload_library_uploaded_files;
--DROP SEQUENCE fcs_migration.application_technical_review_id_seq;
--DROP TABLE fcs_migration.application_technical_reviews;
--DROP SEQUENCE fcs_migration.application_update_id_seq;
--DROP TABLE fcs_migration.application_updates;
--DROP SEQUENCE fcs_migration.application_case_note_id_seq;
--DROP TABLE fcs_migration.application_case_notes;
--DROP SEQUENCE fcs_migration.vent_report_123_month_id_seq;
--DROP TABLE fcs_migration.vent_report_123_months;
--DROP SEQUENCE fcs_migration.vent_report_123_gas_data_id_seq;
--DROP TABLE fcs_migration.vent_report_123_gas_data;
--DROP SEQUENCE fcs_migration.vent_short_term_123_month_id_seq;
--DROP TABLE fcs_migration.vent_short_term_123_months;
--DROP SEQUENCE fcs_migration.vent_annual_123_month_id_seq;
--DROP TABLE fcs_migration.vent_annual_123_months;
--DROP SEQUENCE fcs_migration.vent_id_seq;
--DROP TABLE fcs_migration.vents;
--DROP SEQUENCE fcs_migration.vent_report_month_id_seq;
--DROP TABLE fcs_migration.vent_report_months;
--DROP SEQUENCE fcs_migration.vent_report_period_id_seq;
--DROP TABLE fcs_migration.vent_report_periods;
--DROP SEQUENCE fcs_migration.vent_report_gas_data_id_seq;
--DROP TABLE fcs_migration.vent_report_gas_data;
--DROP SEQUENCE fcs_migration.vent_short_term_month_id_seq;
--DROP TABLE fcs_migration.vent_short_term_months;
--DROP SEQUENCE fcs_migration.vent_annual_month_id_seq;
--DROP TABLE fcs_migration.vent_annual_months;
--DROP SEQUENCE fcs_migration.flare_report_123_month_id_seq;
--DROP TABLE fcs_migration.flare_report_123_months;
--DROP SEQUENCE fcs_migration.flare_report_123_gas_data_id_seq;
--DROP TABLE fcs_migration.flare_report_123_gas_data;
--DROP SEQUENCE fcs_migration.flare_short_term_123_month_id_seq;
--DROP TABLE fcs_migration.flare_short_term_123_months;
--DROP SEQUENCE fcs_migration.flare_annual_123_month_id_seq;
--DROP TABLE fcs_migration.flare_annual_123_months;
--DROP SEQUENCE fcs_migration.flare_id_seq;
--DROP TABLE fcs_migration.flares;
--DROP SEQUENCE fcs_migration.flare_report_month_id_seq;
--DROP TABLE fcs_migration.flare_report_months;
--DROP SEQUENCE fcs_migration.flare_report_period_id_seq;
--DROP TABLE fcs_migration.flare_report_periods;
--DROP SEQUENCE fcs_migration.flare_report_gas_data_id_seq;
--DROP TABLE fcs_migration.flare_report_gas_data;
--DROP SEQUENCE fcs_migration.flare_short_term_month_id_seq;
--DROP TABLE fcs_migration.flare_short_term_months;
--DROP SEQUENCE fcs_migration.flare_annual_month_id_seq;
--DROP TABLE fcs_migration.flare_annual_months;
--DROP SEQUENCE fcs_migration.short_term_production_month_id_seq;
--DROP TABLE fcs_migration.short_term_production_months;
--DROP SEQUENCE fcs_migration.annual_production_month_id_seq;
--DROP TABLE fcs_migration.annual_production_months;
--DROP SEQUENCE fcs_migration.long_term_production_year_id_seq;
--DROP TABLE fcs_migration.long_term_production_years;
--DROP SEQUENCE fcs_migration.application_supporting_information_id_seq;
--DROP TABLE fcs_migration.application_supporting_information;
--DROP SEQUENCE fcs_migration.application_eia_direction_id_seq;
--DROP TABLE fcs_migration.application_eia_directions;
--DROP SEQUENCE fcs_migration.application_flag_id_seq;
--DROP TABLE fcs_migration.application_flags;
--DROP SEQUENCE fcs_migration.application_unit_id_seq;
--DROP TABLE fcs_migration.application_units;
--DROP SEQUENCE fcs_migration.application_asset_licence_id_seq;
--DROP TABLE fcs_migration.application_asset_licences;
--DROP SEQUENCE fcs_migration.application_asset_id_seq;
--DROP TABLE fcs_migration.application_assets;
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
, case_officer_wua_id          INTEGER -- Range 6/Administrator/Revision Admin
, cam_wua_id                   INTEGER -- Range 10
, current_case_owner           VARCHAR2(4000)
, migrated                     VARCHAR2(5) NOT NULL -- always true
);


--
-- consent_lengths
--

CREATE SEQUENCE fcs_migration.consent_length_id_seq;

CREATE TABLE fcs_migration.consent_lengths (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT app_types_fk1_version_id
                         REFERENCES fcs_migration.application_versions
, consent_length         VARCHAR2(4000) NOT NULL
, annual_consent_year    INTEGER
, short_term_start_date  DATE
, short_term_end_date    DATE
, long_term_start_year   INTEGER
, long_term_end_year     INTEGER
);

--
-- application_assets
--

CREATE SEQUENCE fcs_migration.application_asset_id_seq;

CREATE TABLE fcs_migration.application_assets (
  id                         INTEGER PRIMARY KEY
, application_version_id     INTEGER NOT NULL
                             CONSTRAINT app_assets_fk1_version_id
                             REFERENCES fcs_migration.application_versions
, asset_role                 VARCHAR2(4000) NOT NULL
, asset_no                   INTEGER
, asset_operator_ou_id       INTEGER NOT NULL
, cached_asset_operator_name VARCHAR2(4000)
, asset_type                 VARCHAR2(4000)
, asset_id                   INTEGER
, cached_asset_name          VARCHAR2(4000)
);


--
-- application_asset_licences
--

CREATE SEQUENCE fcs_migration.application_asset_licence_id_seq;

CREATE TABLE fcs_migration.application_asset_licences (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT app_asset_licences_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, application_asset_id   INTEGER NOT NULL
                         CONSTRAINT app_asset_licences_fk2_aa_id
                         REFERENCES fcs_migration.application_assets
, licence_id             INTEGER NOT NULL
, cached_licence_ref     VARCHAR2(4000) NOT NULL
);
/

--
-- application_units
--

CREATE SEQUENCE fcs_migration.application_unit_id_seq;

CREATE TABLE fcs_migration.application_units (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT application_units_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, flare_category_unit    VARCHAR2(4000)
, vent_category_unit     VARCHAR2(4000)
, production_oil_unit    VARCHAR2(4000)
, production_gas_unit    VARCHAR2(4000)
, flare_gas_density_unit VARCHAR2(4000)
, flare_gas_content_unit VARCHAR2(4000)
, vent_gas_density_unit  VARCHAR2(4000)
, vent_gas_content_unit  VARCHAR2(4000)
, emission_category_type VARCHAR2(4000)
);


--
-- application_flags
--

CREATE SEQUENCE fcs_migration.application_flag_id_seq;

CREATE TABLE fcs_migration.application_flags (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT app_flags_fk1_version_id
                         REFERENCES fcs_migration.application_versions
, flag_type              VARCHAR2(4000) NOT NULL
, flag_value             VARCHAR2(5) NOT NULL -- true/false
, CONSTRAINT app_flags_uk1_version_id UNIQUE (application_version_id, flag_type)
);

--
-- application_eia_directions
--

CREATE SEQUENCE fcs_migration.application_eia_direction_id_seq;

CREATE TABLE fcs_migration.application_eia_directions (
  id                           INTEGER PRIMARY KEY
, application_version_id       INTEGER NOT NULL
                               CONSTRAINT application_eia_directions_fk1_av_id
                               REFERENCES fcs_migration.application_versions
, have_submitted_eia_direction VARCHAR2(5)
, sat_id                       INTEGER
, cached_sat_ref               VARCHAR2(4000)
, have_eia_direction_to_submit VARCHAR2(5)
, latest_date_to_be_submitted  DATE
, why_no_eia_direction         VARCHAR2(4000)
, for_purpose_of_eia_regs      VARCHAR2(5)
);

--
-- application_supporting_information
--

CREATE SEQUENCE fcs_migration.application_supporting_information_id_seq;

CREATE TABLE fcs_migration.application_supporting_information (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT application_supporting_information_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, notes                  CLOB
, erap_notes             CLOB
);

--
-- long_term_production_years
--

CREATE SEQUENCE fcs_migration.long_term_production_year_id_seq;

CREATE TABLE fcs_migration.long_term_production_years (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT long_term_prod_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, oil_min_value          NUMBER
, oil_max_value          NUMBER
, gas_min_value          NUMBER
, gas_max_value          NUMBER
);

--
-- annual_production_months
--
CREATE SEQUENCE fcs_migration.annual_production_month_id_seq;

CREATE TABLE fcs_migration.annual_production_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT annual_prod_fk1_version_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, oil_min_value          NUMBER
, oil_max_value          NUMBER NOT NULL
, gas_min_value          NUMBER
, gas_max_value          NUMBER NOT NULL
);

--
-- short_term_production_months
--

CREATE SEQUENCE fcs_migration.short_term_production_month_id_seq;

CREATE TABLE fcs_migration.short_term_production_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT short_prod_fk1_version_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, start_date             DATE NOT NULL
, end_date               DATE NOT NULL
, oil_min_value          NUMBER NOT NULL
, oil_max_value          NUMBER NOT NULL
, gas_min_value          NUMBER NOT NULL
, gas_max_value          NUMBER NOT NULL
);

--
-- flare_annual_months
--

CREATE SEQUENCE fcs_migration.flare_annual_month_id_seq;

CREATE TABLE fcs_migration.flare_annual_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_annual_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_a             NUMBER NOT NULL
, category_b             NUMBER NOT NULL
, category_c             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- flare_annual_123_months
--

CREATE SEQUENCE fcs_migration.flare_annual_123_month_id_seq;

CREATE TABLE fcs_migration.flare_annual_123_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_annual_123_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_1             NUMBER NOT NULL
, category_2             NUMBER NOT NULL
, category_3             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- flare_short_term_months
--

CREATE SEQUENCE fcs_migration.flare_short_term_month_id_seq;

CREATE TABLE fcs_migration.flare_short_term_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_short_term_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, start_date             DATE
, end_date               DATE
, category_a             NUMBER NOT NULL
, category_b             NUMBER NOT NULL
, category_c             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- flare_short_term_123_months
--

CREATE SEQUENCE fcs_migration.flare_short_term_123_month_id_seq;

CREATE TABLE fcs_migration.flare_short_term_123_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_short_term_123_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, start_date             DATE
, end_date               DATE
, category_1             NUMBER NOT NULL
, category_2             NUMBER NOT NULL
, category_3             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- flare_report_gas_data
--

CREATE SEQUENCE fcs_migration.flare_report_gas_data_id_seq;

CREATE TABLE fcs_migration.flare_report_gas_data (
  id                                 INTEGER PRIMARY KEY
, application_version_id             INTEGER NOT NULL
                                     CONSTRAINT flare_report_gas_data_fk1_av_id
                                     REFERENCES fcs_migration.application_versions
, category_a_density                 NUMBER NOT NULL
, category_a_inert_percentage        NUMBER NOT NULL
, category_a_hydro_percentage        NUMBER NOT NULL
, category_b_density                 NUMBER NOT NULL
, category_b_inert_percentage        NUMBER NOT NULL
, category_b_hydro_percentage        NUMBER NOT NULL
, category_c_density                 NUMBER NOT NULL
, category_c_inert_percentage        NUMBER NOT NULL
, category_c_hydro_percentage        NUMBER NOT NULL
, evaluated_per_category             VARCHAR2(5)  -- true/false
, evaluated_per_category_explanation VARCHAR2(4000)
);

--
-- flare_report_123_gas_data
--

CREATE SEQUENCE fcs_migration.flare_report_123_gas_data_id_seq;

CREATE TABLE fcs_migration.flare_report_123_gas_data (
  id                                 INTEGER PRIMARY KEY
, application_version_id             INTEGER NOT NULL
                                     CONSTRAINT flare_report_123_gas_data_fk1_av_id
                                     REFERENCES fcs_migration.application_versions
, category_1_density                 NUMBER
, category_1_inert_percentage        NUMBER
, category_1_hydro_percentage        NUMBER
, category_2_density                 NUMBER
, category_2_inert_percentage        NUMBER
, category_2_hydro_percentage        NUMBER
, category_3_density                 NUMBER
, category_3_inert_percentage        NUMBER
, category_3_hydro_percentage        NUMBER
);

--
-- flare_report_periods
--

CREATE SEQUENCE fcs_migration.flare_report_period_id_seq;

CREATE TABLE fcs_migration.flare_report_periods (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_report_periods_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, report_end_month       VARCHAR2(4000) NOT NULL
, report_end_year        INTEGER NOT NULL
);

--
-- flare_report_months
--

CREATE SEQUENCE fcs_migration.flare_report_month_id_seq;

CREATE TABLE fcs_migration.flare_report_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_report_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_a             NUMBER NOT NULL
, category_b             NUMBER NOT NULL
, category_c             NUMBER NOT NULL
, shut_down_days         INTEGER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- flare_report_123_months
--

CREATE SEQUENCE fcs_migration.flare_report_123_month_id_seq;

CREATE TABLE fcs_migration.flare_report_123_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flare_report_123_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_1             NUMBER NOT NULL
, category_2             NUMBER NOT NULL
, category_3             NUMBER NOT NULL
, shut_down_days         INTEGER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- flares
--

CREATE SEQUENCE fcs_migration.flare_id_seq;

CREATE TABLE fcs_migration.flares (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT flares_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, flare_no               INTEGER NOT NULL
, flare_type             VARCHAR2(4000) NOT NULL
, description            VARCHAR2(4000)
, metered_flag           VARCHAR2(5)
, comments               VARCHAR2(4000)
);

--
-- vent_annual_months
--

CREATE SEQUENCE fcs_migration.vent_annual_month_id_seq;

CREATE TABLE fcs_migration.vent_annual_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_annual_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_a             NUMBER NOT NULL
, category_b             NUMBER NOT NULL
, category_c             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- vent_annual_123_months
--

CREATE SEQUENCE fcs_migration.vent_annual_123_month_id_seq;

CREATE TABLE fcs_migration.vent_annual_123_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_annual_123_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_1             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- vent_short_term_months
--

CREATE SEQUENCE fcs_migration.vent_short_term_month_id_seq;

CREATE TABLE fcs_migration.vent_short_term_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_short_term_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, start_date             DATE
, end_date               DATE
, category_a             NUMBER NOT NULL
, category_b             NUMBER NOT NULL
, category_c             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- vent_short_term_123_months
--

CREATE SEQUENCE fcs_migration.vent_short_term_123_month_id_seq;

CREATE TABLE fcs_migration.vent_short_term_123_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_short_term_123_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, start_date             DATE
, end_date               DATE
, category_1             NUMBER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- vent_report_gas_data
--

CREATE SEQUENCE fcs_migration.vent_report_gas_data_id_seq;

CREATE TABLE fcs_migration.vent_report_gas_data (
  id                                 INTEGER PRIMARY KEY
, application_version_id             INTEGER NOT NULL
                                     CONSTRAINT vent_report_gas_data_fk1_av_id
                                     REFERENCES fcs_migration.application_versions
, category_a_density                 NUMBER NOT NULL
, category_a_inert_percentage        NUMBER NOT NULL
, category_a_hydro_percentage        NUMBER NOT NULL
, category_b_density                 NUMBER NOT NULL
, category_b_inert_percentage        NUMBER NOT NULL
, category_b_hydro_percentage        NUMBER NOT NULL
, category_c_density                 NUMBER NOT NULL
, category_c_inert_percentage        NUMBER NOT NULL
, category_c_hydro_percentage        NUMBER NOT NULL
, evaluated_per_category             VARCHAR2(5)  -- true/false
, evaluated_per_category_explanation VARCHAR2(4000)
);

--
-- vent_report_123_gas_data
--

CREATE SEQUENCE fcs_migration.vent_report_123_gas_data_id_seq;

CREATE TABLE fcs_migration.vent_report_123_gas_data (
  id                                 INTEGER PRIMARY KEY
, application_version_id             INTEGER NOT NULL
                                     CONSTRAINT vent_report_123_gas_data_fk1_av_id
                                     REFERENCES fcs_migration.application_versions
, category_1_density                 NUMBER
, category_1_inert_percentage        NUMBER
, category_1_hydro_percentage        NUMBER
);

--
-- vent_report_periods
--

CREATE SEQUENCE fcs_migration.vent_report_period_id_seq;

CREATE TABLE fcs_migration.vent_report_periods (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_report_periods_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, report_end_month       VARCHAR2(4000) NOT NULL
, report_end_year        INTEGER NOT NULL
);


--
-- vent_report_months
--

CREATE SEQUENCE fcs_migration.vent_report_month_id_seq;

CREATE TABLE fcs_migration.vent_report_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_report_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_a             NUMBER NOT NULL
, category_b             NUMBER NOT NULL
, category_c             NUMBER NOT NULL
, shut_down_days         INTEGER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- vent_report_123_months
--

CREATE SEQUENCE fcs_migration.vent_report_123_month_id_seq;

CREATE TABLE fcs_migration.vent_report_123_months (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vent_report_123_months_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, year                   INTEGER NOT NULL
, month                  VARCHAR2(4000) NOT NULL
, category_1             NUMBER NOT NULL
, shut_down_days         INTEGER NOT NULL
, comments               VARCHAR2(4000)
);

--
-- vents
--

CREATE SEQUENCE fcs_migration.vent_id_seq;

CREATE TABLE fcs_migration.vents (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT vents_fk1_av_id
                         REFERENCES fcs_migration.application_versions
, vent_no                INTEGER NOT NULL
, vent_type              VARCHAR2(4000) NOT NULL
, description            VARCHAR2(4000)
, metered_flag           VARCHAR2(5)
, comments               VARCHAR2(4000)
);

--
-- case_notes
--

CREATE SEQUENCE fcs_migration.application_case_note_id_seq;

CREATE TABLE fcs_migration.application_case_notes (
  id                     INTEGER PRIMARY KEY
, application_version_id INTEGER NOT NULL
                         CONSTRAINT app_case_notes_fk1_version_id
                         REFERENCES fcs_migration.application_versions
, added_by_wua_id        INTEGER NOT NULL
, added_date_time        DATE NOT NULL
, case_note_text         CLOB NOT NULL
);

--
-- application_updates
--
CREATE SEQUENCE fcs_migration.application_update_id_seq;

CREATE TABLE fcs_migration.application_updates (
  id                              INTEGER PRIMARY KEY
, application_version_id          INTEGER NOT NULL
                                  CONSTRAINT app_updates_fk1_version_id
                                  REFERENCES fcs_migration.application_versions
, requested_by_wua_id             INTEGER NOT NULL
, requested_date_time             DATE NOT NULL
, request_text                    CLOB NOT NULL
, deadline_date_time              DATE
, responded_by_wua_id             INTEGER
, responded_date_time             DATE
, response_text                   CLOB
, response_type                   VARCHAR2(4000)
, application_update_status       VARCHAR2(4000) NOT NULL
, response_application_version_id INTEGER
                                  CONSTRAINT app_updates_fk2_resp_version_id
                                  REFERENCES fcs_migration.application_versions
);

--
-- application_technical_reviews
--

CREATE SEQUENCE fcs_migration.application_technical_review_id_seq;

CREATE TABLE fcs_migration.application_technical_reviews (
  id                              INTEGER PRIMARY KEY
, request_application_version_id  INTEGER NOT NULL
                                  CONSTRAINT app_technical_reviews_fk1_version_id
                                  REFERENCES fcs_migration.application_versions
, requested_by_wua_id             INTEGER NOT NULL
, requested_date_time             DATE NOT NULL
, request_text                    VARCHAR2(4000)
, deadline_date_time              DATE
, technical_reviewer_wua_id       INTEGER
, responded_by_wua_id             INTEGER
, responded_date_time             DATE
, response_text                   CLOB
, response_type                   VARCHAR2(4000)
, technical_review_status         VARCHAR2(4000) NOT NULL
, response_application_version_id INTEGER
                                  CONSTRAINT application_technical_reviews_response_application_version_fkey
                                  REFERENCES fcs_migration.application_versions
);

--
-- file_upload_library_uploaded_files
--
CREATE TABLE fcs_migration.file_upload_library_uploaded_files (
  id             VARCHAR2(4000) PRIMARY KEY -- this is a UUID (might not need here)
, bucket         VARCHAR2(4000) NOT NULL -- field-consents
, key            VARCHAR2(4000) NOT NULL -- an AWS S3 UUID ?
, name           VARCHAR2(4000) NOT NULL -- file name e.g. test1.txt
, content_type   VARCHAR2(4000) NOT NULL -- file type e.g. text/plain application/vnd.ms-excel application/pdf image/jpeg
, content_length INTEGER NOT NULL -- file size
, uploaded_at    DATE NOT NULL
, usage_id       VARCHAR2(4000) -- the application version id
, usage_type     VARCHAR2(4000) -- will be ApplicationVersion for supporting docs
, document_type  VARCHAR2(4000) -- e.g. supporting-document
, description    VARCHAR2(4000) -- file description 
, uploaded_by    VARCHAR2(4000) -- wua id
);
