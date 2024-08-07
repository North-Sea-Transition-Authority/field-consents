-- Consent Breaches data
CREATE TABLE application_consent_breaches (
    id                      SERIAL PRIMARY KEY,
    consent_id              INTEGER NOT NULL,
    added_by_wua_id         INTEGER NOT NULL,
    added_date_time         TIMESTAMPTZ NOT NULL,
    breach_text             TEXT NOT NULL,

    CONSTRAINT application_consent_breaches_fk1_consent_id
        FOREIGN KEY (consent_id)
            REFERENCES application_consents (id)
);

CREATE INDEX application_consent_breaches_idx1_consent_id ON application_consent_breaches(consent_id);

-- Consent Breaches audit data
CREATE TABLE application_consent_breaches_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      INTEGER,
    consent_id              INTEGER,
    added_by_wua_id         INTEGER,
    added_date_time         TIMESTAMPTZ,
    breach_text             TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_consent_breaches_aud_rev ON application_consent_breaches_aud(rev);
