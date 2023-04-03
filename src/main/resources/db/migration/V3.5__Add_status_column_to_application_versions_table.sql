ALTER TABLE application_versions
    ADD COLUMN status               TEXT,
    ADD COLUMN created_date_time    TIMESTAMP,
    ADD COLUMN created_by_wua_id    INTEGER,
    ADD COLUMN submitted_date_time  TIMESTAMP,
    ADD COLUMN submitted_by_wua_id  INTEGER;

UPDATE application_versions
SET status = 'IN_PROGRESS'
WHERE status IS NULL;

ALTER TABLE application_versions
ALTER COLUMN status SET NOT NULL;

UPDATE application_versions
SET created_date_time = now()
WHERE created_date_time IS NULL;

ALTER TABLE application_versions
ALTER COLUMN created_date_time SET NOT NULL;

UPDATE application_versions
SET created_by_wua_id = 1
WHERE created_by_wua_id IS NULL;

ALTER TABLE application_versions
ALTER COLUMN created_by_wua_id SET NOT NULL;