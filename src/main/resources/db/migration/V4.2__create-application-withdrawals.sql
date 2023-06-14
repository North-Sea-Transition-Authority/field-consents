CREATE TABLE IF NOT EXISTS application_withdrawals (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    requested_by_wua_id     INTEGER NOT NULL,
    requested_date_time     TIMESTAMP NOT NULL,
    request_text            TEXT NOT NULL,
    withdrawal_status       TEXT NOT NULL,
    responded_by_wua_id     INTEGER,
    responded_date_time     TIMESTAMP,
    response_text           TEXT,
    CONSTRAINT app_withdrawals_fk1_version_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX app_withdrawals_idx1_version_id ON application_withdrawals(application_version_id);
