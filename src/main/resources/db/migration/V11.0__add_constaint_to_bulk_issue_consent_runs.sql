ALTER TABLE bulk_issue_consents_tasks
    ADD CONSTRAINT bulk_issue_consents_tasks_fk1_bulk_issue_consent_run_id
        FOREIGN KEY (bulk_issue_consent_run_id)
            REFERENCES bulk_issue_consent_runs (id);
