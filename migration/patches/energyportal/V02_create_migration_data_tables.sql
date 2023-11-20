
--DROP SEQUENCE fcs_migration.flare_report_month_id_seq;
--DROP TABLE fcs_migration.flare_report_months;
--DROP SEQUENCE fcs_migration.flare_report_period_id_seq;
--DROP TABLE fcs_migration.flare_report_periods;
--DROP SEQUENCE fcs_migration.flare_report_gas_data_id_seq;
--DROP TABLE fcs_migration.flare_report_gas_data;
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
, category_a             NUMERIC NOT NULL
, category_b             NUMERIC NOT NULL
, category_c             NUMERIC NOT NULL
, shut_down_days         INTEGER NOT NULL
, comments               VARCHAR2(4000)
);