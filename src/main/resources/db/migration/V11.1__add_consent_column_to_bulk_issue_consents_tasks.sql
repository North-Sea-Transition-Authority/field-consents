ALTER TABLE bulk_issue_consents_tasks ADD COLUMN consent_id INTEGER;

ALTER TABLE bulk_issue_consents_tasks_aud
    ADD COLUMN consent_id INTEGER,
    ADD CONSTRAINT bulk_issue_consents_tasks_fk2_consent_id
        FOREIGN KEY (consent_id)
            REFERENCES application_consents (id);

CREATE INDEX bulk_issue_consents_tasks_application_consent_idx ON bulk_issue_consents_tasks(consent_id);