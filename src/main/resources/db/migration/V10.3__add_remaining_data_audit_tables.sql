-- Annual production months audit data
CREATE TABLE annual_production_months_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    year                    INTEGER,
    month                   TEXT,
    oil_min_value           NUMERIC,
    oil_max_value           NUMERIC,
    gas_min_value           NUMERIC,
    gas_max_value           NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_annual_production_months_aud_rev ON annual_production_months_aud(rev);

-- Application asset licences audit data
CREATE TABLE application_asset_licences_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    application_asset_id    INTEGER,
    licence_id              INTEGER,
    cached_licence_ref      TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_asset_licences_aud_rev ON application_asset_licences_aud(rev);

-- Application assets audit data
CREATE TABLE application_assets_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    asset_role                  TEXT,
    asset_no                    INTEGER,
    asset_operator_ou_id        INTEGER,
    cached_asset_operator_name  TEXT,
    asset_type                  TEXT,
    asset_id                    INTEGER,
    cached_asset_name           TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_assets_aud_rev ON application_assets_aud(rev);

-- Application case notes audit data
CREATE TABLE application_case_notes_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    added_by_wua_id         INTEGER,
    added_date_time         TIMESTAMPTZ,
    case_note_text          TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_case_notes_aud_rev ON application_case_notes_aud(rev);

-- Application consent document generation data audit data
CREATE TABLE application_consent_document_generation_data_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    consent_id                  INTEGER,
    document_template_mnemonic  TEXT,
    document_title              TEXT,
    pdf_html_content            TEXT,
    pdf_mail_merge_data         JSONB,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_consent_document_generation_aud_data_rev ON application_consent_document_generation_data_aud(rev);

-- Application units audit data
CREATE TABLE application_units_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    flare_category_unit         TEXT,
    vent_category_unit          TEXT,
    production_oil_unit         TEXT,
    production_gas_unit         TEXT,
    flare_gas_density_unit      TEXT,
    flare_gas_content_unit      TEXT,
    vent_gas_density_unit       TEXT,
    vent_gas_content_unit       TEXT,
    emission_category_type      TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_units_aud_rev ON application_units_aud(rev);

-- Application consultation further information audit data
CREATE TABLE application_consultation_further_information_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    consultation_id         INTEGER,
    requested_at_datetime   TIMESTAMPTZ,
    requested_by_wua_id     INTEGER,
    request_text            TEXT,
    status                  TEXT,
    responded_at_datetime   TIMESTAMPTZ,
    responded_by_wua_id     INTEGER,
    response_text           TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_further_information_aud_rev ON application_consultation_further_information_aud(rev);

-- Application eia directions audit data
CREATE TABLE application_eia_directions_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    have_submitted_eia_direction    BOOLEAN,
    sat_id                          INTEGER,
    cached_sat_ref                  TEXT,
    have_eia_direction_to_submit    BOOLEAN,
    latest_date_to_be_submitted     DATE,
    why_no_eia_direction            TEXT,
    for_purpose_of_eia_regs         BOOLEAN,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_eia_directions_aud_rev ON application_eia_directions_aud(rev);

-- Application other legacy data audit data
CREATE TABLE application_other_legacy_data_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    increase_in_production          BOOLEAN,
    es_reference                    TEXT,
    uplift_percentage               INTEGER,
    field_location                  TEXT,
    previous_year_consent_history   NUMERIC,
    previous_year_actuals           NUMERIC,
    terminal_name                   TEXT,
    terminal_location               TEXT,
    project_under_eia_regs          BOOLEAN,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_other_legacy_data_aud_rev ON application_other_legacy_data_aud(rev);

-- Application rationale audit data
CREATE TABLE application_rationale_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    rationale_type          TEXT,
    comment                 TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_rationale_aud_rev ON application_rationale_aud(rev);

-- Application supporting information audit data
CREATE TABLE application_supporting_information_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    notes                   TEXT,
    erap_notes              TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_supporting_information_aud_rev ON application_supporting_information_aud(rev);

-- Application withdrawals audit data
CREATE TABLE application_withdrawals_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    requested_by_wua_id     INTEGER,
    requested_date_time     TIMESTAMPTZ,
    request_text            TEXT,
    withdrawal_status       TEXT,
    responded_by_wua_id     INTEGER,
    responded_date_time     TIMESTAMPTZ,
    response_text           TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_withdrawals_aud_rev ON application_withdrawals_aud(rev);

-- Application work area priorities audit data
CREATE TABLE application_work_area_priorities_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    work_area_priority_group        TEXT,
    work_area_priority_reason       TEXT,
    work_area_priority_date_time    TIMESTAMPTZ,
    work_area_priority_by_wua_id    INTEGER,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_work_area_priorities_aud_rev ON application_work_area_priorities_aud(rev);

-- Application audit data
CREATE TABLE applications_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    type                    TEXT,
    created_date            TIMESTAMPTZ,
    created_by_wua_id       INTEGER,
    variation_no            INTEGER,
    application_no          INTEGER,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_applications_aud_rev ON applications_aud(rev);

-- Consent lengths audit data
CREATE TABLE consent_lengths_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    consent_length                  TEXT,
    annual_consent_year             INTEGER,
    short_term_start_date           DATE,
    short_term_end_date             DATE,
    long_term_start_year            INTEGER,
    long_term_end_year              INTEGER,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_consent_lengths_aud_rev ON consent_lengths_aud(rev);

-- Flare annual 123 months audit data
CREATE TABLE flare_annual_123_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_1                      NUMERIC,
    category_2                      NUMERIC,
    category_3                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_annual_123_months_aud_rev ON flare_annual_123_months_aud(rev);

-- Flare report 123 gas data audit data
CREATE TABLE flare_report_123_gas_data_aud (
    rev                                     SERIAL,
    revtype                                 NUMERIC,
    id                                      INTEGER,
    application_version_id                  INTEGER,
    category_1_density                      NUMERIC,
    category_1_inert_percentage             NUMERIC,
    category_1_hydro_percentage             NUMERIC,
    category_2_density                      NUMERIC,
    category_2_inert_percentage             NUMERIC,
    category_2_hydro_percentage             NUMERIC,
    category_3_density                      NUMERIC,
    category_3_inert_percentage             NUMERIC,
    category_3_hydro_percentage             NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_report_123_gas_data_aud_rev ON flare_report_123_gas_data_aud(rev);

-- Flare report 123 months audit data
CREATE TABLE flare_report_123_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_1                      NUMERIC,
    category_2                      NUMERIC,
    category_3                      NUMERIC,
    shut_down_days                  INTEGER,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_report_123_months_aud_rev ON flare_report_123_months_aud(rev);

-- Flare short term 123 months audit data
CREATE TABLE flare_short_term_123_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    start_date                      DATE,
    end_date                        DATE,
    category_1                      NUMERIC,
    category_2                      NUMERIC,
    category_3                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_short_term_123_months_aud_rev ON flare_short_term_123_months_aud(rev);

-- Flare annual months audit data
CREATE TABLE flare_annual_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_a                      NUMERIC,
    category_b                      NUMERIC,
    category_c                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_annual_months_aud_rev ON flare_annual_months_aud(rev);

-- Flare long term year audit data
CREATE TABLE flare_long_term_years_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    year                        INTEGER,
    gas                         NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_long_term_years_aud_rev ON flare_long_term_years_aud(rev);

-- Flare report gas data audit data
CREATE TABLE flare_report_gas_data_aud (
    rev                                     SERIAL,
    revtype                                 NUMERIC,
    id                                      INTEGER,
    application_version_id                  INTEGER,
    category_a_density                      NUMERIC,
    category_a_inert_percentage             NUMERIC,
    category_a_hydro_percentage             NUMERIC,
    category_b_density                      NUMERIC,
    category_b_inert_percentage             NUMERIC,
    category_b_hydro_percentage             NUMERIC,
    category_c_density                      NUMERIC,
    category_c_inert_percentage             NUMERIC,
    category_c_hydro_percentage             NUMERIC,
    evaluated_per_category                  BOOLEAN,
    evaluated_per_category_explanation      TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_report_gas_data_aud_rev ON flare_report_gas_data_aud(rev);

-- Flare report months audit data
CREATE TABLE flare_report_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_a                      NUMERIC,
    category_b                      NUMERIC,
    category_c                      NUMERIC,
    shut_down_days                  INTEGER,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_report_months_aud_rev ON flare_report_months_aud(rev);

-- Flare report periods audit data
CREATE TABLE flare_report_periods_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    report_end_month            TEXT,
    report_end_year             INTEGER,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_report_periods_aud_rev ON flare_report_periods_aud(rev);

-- Flare short term months audit data
CREATE TABLE flare_short_term_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    start_date                      DATE,
    end_date                        DATE,
    category_a                      NUMERIC,
    category_b                      NUMERIC,
    category_c                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flare_short_term_months_aud_rev ON flare_short_term_months_aud(rev);

-- Flares audit data
CREATE TABLE flares_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    flare_no                    INTEGER,
    flare_type                  TEXT,
    description                 TEXT,
    metered_flag                BOOLEAN,
    comments                    TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_flares_aud_rev ON flares_aud(rev);

-- Vent annual months audit data
CREATE TABLE vent_annual_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_a                      NUMERIC,
    category_b                      NUMERIC,
    category_c                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_annual_months_aud_rev ON vent_annual_months_aud(rev);

-- Vent report gas data audit data
CREATE TABLE vent_report_gas_data_aud (
    rev                                     SERIAL,
    revtype                                 NUMERIC,
    id                                      INTEGER,
    application_version_id                  INTEGER,
    category_a_density                      NUMERIC,
    category_a_inert_percentage             NUMERIC,
    category_a_hydro_percentage             NUMERIC,
    category_b_density                      NUMERIC,
    category_b_inert_percentage             NUMERIC,
    category_b_hydro_percentage             NUMERIC,
    category_c_density                      NUMERIC,
    category_c_inert_percentage             NUMERIC,
    category_c_hydro_percentage             NUMERIC,
    evaluated_per_category                  BOOLEAN,
    evaluated_per_category_explanation      TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_report_gas_data_aud_rev ON vent_report_gas_data_aud(rev);

-- Vent report months audit data
CREATE TABLE vent_report_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_a                      NUMERIC,
    category_b                      NUMERIC,
    category_c                      NUMERIC,
    shut_down_days                  INTEGER,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_report_months_aud_rev ON vent_report_months_aud(rev);

-- Vent report periods audit data
CREATE TABLE vent_report_periods_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    report_end_month            TEXT,
    report_end_year             INTEGER,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_report_periods_aud_rev ON vent_report_periods_aud(rev);

-- Vent short term months audit data
CREATE TABLE vent_short_term_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    start_date                      DATE,
    end_date                        DATE,
    category_a                      NUMERIC,
    category_b                      NUMERIC,
    category_c                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_short_term_months_aud_rev ON vent_short_term_months_aud(rev);

-- Vents audit data
CREATE TABLE vents_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    vent_no                     INTEGER,
    vent_type                   TEXT,
    description                 TEXT,
    metered_flag                BOOLEAN,
    comments                    TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vents_aud_rev ON vents_aud(rev);

-- Vent annual 123 months audit data
CREATE TABLE vent_annual_123_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_1                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_annual_123_months_aud_rev ON vent_annual_123_months_aud(rev);

-- Vent report 123 gas data audit data
CREATE TABLE vent_report_123_gas_data_aud (
    rev                                     SERIAL,
    revtype                                 NUMERIC,
    id                                      INTEGER,
    application_version_id                  INTEGER,
    category_1_density                      NUMERIC,
    category_1_inert_percentage             NUMERIC,
    category_1_hydro_percentage             NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_report_123_gas_data_aud_rev ON vent_report_123_gas_data_aud(rev);

-- Vent report 123 months audit data
CREATE TABLE vent_report_123_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    category_1                      NUMERIC,
    shut_down_days                  INTEGER,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_report_123_months_aud_rev ON vent_report_123_months_aud(rev);

-- Vent short term 123 months audit data
CREATE TABLE vent_short_term_123_months_aud (
    rev                             SERIAL,
    revtype                         NUMERIC,
    id                              INTEGER,
    application_version_id          INTEGER,
    year                            INTEGER,
    month                           TEXT,
    start_date                      DATE,
    end_date                        DATE,
    category_1                      NUMERIC,
    comments                        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_short_term_123_months_aud_rev ON vent_short_term_123_months_aud(rev);

-- Vent long term year audit data
CREATE TABLE vent_long_term_years_aud (
    rev                         SERIAL,
    revtype                     NUMERIC,
    id                          INTEGER,
    application_version_id      INTEGER,
    year                        INTEGER,
    gas                         NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_vent_long_term_years_aud_rev ON vent_long_term_years_aud(rev);

-- Long term production months audit data
CREATE TABLE long_term_production_years_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    year                    INTEGER,
    oil_min_value           NUMERIC,
    oil_max_value           NUMERIC,
    gas_min_value           NUMERIC,
    gas_max_value           NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_long_term_production_years_aud_rev ON long_term_production_years_aud(rev);

-- Short term production months audit data
CREATE TABLE short_term_production_months_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    application_version_id  INTEGER,
    year                    INTEGER,
    month                   TEXT,
    start_date              DATE,
    end_date                DATE,
    oil_min_value           NUMERIC,
    oil_max_value           NUMERIC,
    gas_min_value           NUMERIC,
    gas_max_value           NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_short_term_production_months_aud_rev ON short_term_production_months_aud(rev);

-- Team member roles audit data
CREATE TABLE team_member_roles_aud (
    rev         SERIAL,
    revtype     NUMERIC,
    id          INTEGER,
    wua_id      INTEGER,
    team_id     INTEGER,
    role        TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_team_member_roles_aud_rev ON team_member_roles_aud(rev);

-- Teams audit data
CREATE TABLE teams_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    type                    VARCHAR,
    display_name            TEXT,
    organisation_group_id   INTEGER,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_teams_aud_rev ON teams_aud(rev);

ALTER TABLE application_consultations_aud
ADD COLUMN consultation_team_id INTEGER REFERENCES teams(id);
