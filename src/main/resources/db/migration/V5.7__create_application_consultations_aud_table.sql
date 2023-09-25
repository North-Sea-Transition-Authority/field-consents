CREATE TABLE application_consultations_aud (
    rev                            SERIAL,
    revtype                        NUMERIC,
    id                             INT,
    request_application_version_id INT,
    request_deadline               TIMESTAMPTZ,
    requested_at_datetime          TIMESTAMPTZ,
    requested_by_wua_id            INT,
    responder_wua_id               INTEGER,
    status                         TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_application_consultations_aud_rev ON application_consultations_aud(rev);
