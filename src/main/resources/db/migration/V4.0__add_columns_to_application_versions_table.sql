ALTER TABLE application_versions
    ADD COLUMN case_officer_wua_id INTEGER;

ALTER TABLE application_versions_aud
    ADD COLUMN case_officer_wua_id INTEGER;
