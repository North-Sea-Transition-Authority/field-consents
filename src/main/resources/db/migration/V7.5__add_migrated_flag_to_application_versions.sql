ALTER TABLE application_versions
    ADD COLUMN migrated BOOLEAN;

UPDATE application_versions
SET migrated = FALSE;

ALTER TABLE application_versions
    ALTER COLUMN migrated SET NOT NULL;

ALTER TABLE application_versions_aud
    ADD COLUMN migrated BOOLEAN;

UPDATE application_versions_aud
SET migrated = FALSE;
