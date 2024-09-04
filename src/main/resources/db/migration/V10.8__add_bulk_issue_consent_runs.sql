CREATE TABLE bulk_issue_consent_runs (
    id                  UUID    PRIMARY KEY,
    issued_by_wua_id    BIGINT  NOT NULL
);

ALTER TABLE bulk_issue_consents_tasks DROP COLUMN created_by_wua_id;

ALTER TABLE bulk_issue_consents_tasks_aud DROP COLUMN created_by_wua_id;

ALTER TABLE bulk_issue_consents_tasks
    ADD COLUMN bulk_issue_consent_run_id UUID,
    ADD CONSTRAINT bulk_issue_consents_tasks_fk1_bulk_issue_consent_run_id
        FOREIGN KEY (bulk_issue_consent_run_id)
            REFERENCES bulk_issue_consent_runs (id);

ALTER TABLE bulk_issue_consents_tasks_aud ADD COLUMN bulk_issue_consent_run_id UUID;

CREATE INDEX bulk_issue_consents_tasks_bulk_issue_consent_run_idx ON bulk_issue_consents_tasks(bulk_issue_consent_run_id);

CREATE TABLE bulk_issue_consent_runs_aud (
    rev                     SERIAL,
    revtype                 NUMERIC,
    id                      UUID,
    issued_by_wua_id        BIGINT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);