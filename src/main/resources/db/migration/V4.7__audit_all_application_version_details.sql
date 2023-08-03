ALTER TABLE application_versions_aud
    ADD COLUMN application_id                   INTEGER,
    ADD COLUMN version_no                       INTEGER,
    ADD COLUMN primary_operator_ou_id           INTEGER,
    ADD COLUMN cached_primary_operator_name     TEXT,
    ADD COLUMN created_date_time                TIMESTAMP,
    ADD COLUMN created_by_wua_id                INTEGER,
    ADD COLUMN submitted_date_time              TIMESTAMP,
    ADD COLUMN submitted_by_wua_id              INTEGER;
