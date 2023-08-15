ALTER TABLE application_updates
    ADD COLUMN response_application_version_id INTEGER,
    ADD CONSTRAINT app_updates_fk2_resp_version_id
        FOREIGN KEY (response_application_version_id)
            REFERENCES application_versions (id);

CREATE INDEX app_updates_idx2_resp_version_id ON application_updates(response_application_version_id);

ALTER TABLE application_updates_aud
    ADD COLUMN response_application_version_id INTEGER;
