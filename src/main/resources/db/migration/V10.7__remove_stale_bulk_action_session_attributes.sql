DELETE
FROM spring_session_attributes
WHERE attribute_name = 'bulkCaseActionSelectedApplicationsForm'
OR attribute_name = 'bulkCaseActionSearchFilterForm';

CREATE TABLE bulk_issue_consents_tasks (
    id                     UUID PRIMARY KEY,
    application_version_id INTEGER     NOT NULL REFERENCES application_versions(id),
    created_at             TIMESTAMPTZ NOT NULL,
    started_at             TIMESTAMPTZ,
    finished_at            TIMESTAMPTZ,
    created_by_wua_id      BIGINT      NOT NULL,
    error_details          TEXT
);

CREATE INDEX bulk_issue_consents_tasks_application_version_idx ON bulk_issue_consents_tasks(application_version_id);

CREATE TABLE bulk_issue_consents_tasks_aud (
    rev                    SERIAL,
    revtype                NUMERIC,
    id                     UUID,
    application_version_id INTEGER,
    created_at             TIMESTAMPTZ,
    started_at             TIMESTAMPTZ,
    finished_at            TIMESTAMPTZ,
    created_by_wua_id      BIGINT,
    error_details          TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);