-- Application Consent Data
CREATE TABLE application_consent_data_aud (
    rev                                              SERIAL,
    revtype                                          NUMERIC,
    id                                               INTEGER,
    application_id                                   INTEGER,
    consent_start_date                               DATE,
    consent_end_date                                 DATE,
    short_term_or_annual_production_min_oil          NUMERIC,
    short_term_or_annual_production_max_oil          NUMERIC,
    short_term_or_annual_production_min_gas          NUMERIC,
    short_term_or_annual_production_max_gas          NUMERIC,
    emission_daily_average                           NUMERIC,
    long_term_production_consent_schedule_start_date DATE,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_consent_data_aud_rev ON application_consent_data_aud(rev);

-- Application Consent Data Long Term Production Figures
CREATE TABLE application_consent_data_long_term_production_figures_aud (
    rev            SERIAL,
    revtype        NUMERIC,
    id             INTEGER,
    application_id INTEGER,
    year           INTEGER,
    min_oil        NUMERIC,
    max_oil        NUMERIC,
    min_gas        NUMERIC,
    max_gas        NUMERIC,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_app_consent_data_long_term_production_figures_aud_rev ON application_consent_data_long_term_production_figures_aud(rev);

-- Application Consent Field Equity Partners
CREATE TABLE application_consent_field_equity_partners_aud (
    rev                    SERIAL,
    revtype                NUMERIC,
    id                     INTEGER,
    application_consent_id INTEGER,
    organisation_unit_id   INTEGER,
    organisation_name      TEXT,
    registered_number      TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_consent_field_equity_partners_aud_rev ON application_consent_field_equity_partners_aud(rev);

-- Application Consents
CREATE TABLE application_consents_aud (
    rev              SERIAL,
    revtype          NUMERIC,
    id               INTEGER,
    application_id   INTEGER,
    issued_by_wua_id INTEGER,
    issued_timestamp TIMESTAMPTZ,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_consents_aud_rev ON application_consents_aud(rev);
