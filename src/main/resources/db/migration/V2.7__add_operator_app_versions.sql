ALTER TABLE application_versions
ADD COLUMN primary_operator_ou_id INTEGER,
ADD COLUMN cached_primary_operator_name TEXT;

UPDATE application_versions
SET primary_operator_ou_id = 0;

ALTER TABLE application_versions
ALTER COLUMN primary_operator_ou_id SET NOT NULL;
