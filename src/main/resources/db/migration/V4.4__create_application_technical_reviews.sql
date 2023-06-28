CREATE TABLE application_technical_reviews (
    id                        SERIAL PRIMARY KEY,
    application_version_id    INTEGER NOT NULL,
    requested_by_wua_id       INTEGER NOT NULL,
    requested_date_time       TIMESTAMP NOT NULL,
    request_text              TEXT,
    deadline_date_time        TIMESTAMP NOT NULL,
    technical_reviewer_wua_id INTEGER NOT NULL,
    responded_by_wua_id       INTEGER,
    responded_date_time       TIMESTAMP,
    response_text             TEXT,
    response_type             TEXT,
    technical_review_status   TEXT NOT NULL,
    CONSTRAINT app_technical_reviews_fk1_version_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX app_technical_reviews_idx1_version_id ON application_technical_reviews(application_version_id);

CREATE TABLE application_technical_reviews_aud (
    rev                       SERIAL,
    revtype                   NUMERIC,
    id                        INTEGER,
    application_version_id    INTEGER,
    requested_by_wua_id       INTEGER,
    requested_date_time       TIMESTAMP,
    request_text              TEXT,
    deadline_date_time        TIMESTAMP,
    technical_reviewer_wua_id INTEGER,
    responded_by_wua_id       INTEGER,
    responded_date_time       TIMESTAMP,
    response_text             TEXT,
    response_type             TEXT,
    technical_review_status   TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_app_technical_reviews_aud_rev ON application_technical_reviews_aud(rev);
