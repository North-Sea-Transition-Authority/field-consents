ALTER TABLE application_versions
    ADD COLUMN cam_wua_id INTEGER,
    ADD COLUMN current_case_owner TEXT;

UPDATE application_versions
SET current_case_owner = 'CASE_OFFICER'
WHERE case_officer_wua_id IS NOT NULL;

ALTER TABLE application_versions_aud
    ADD COLUMN cam_wua_id INTEGER,
    ADD COLUMN current_case_owner TEXT;

UPDATE application_versions_aud
SET current_case_owner = 'CASE_OFFICER'
WHERE case_officer_wua_id IS NOT NULL;
