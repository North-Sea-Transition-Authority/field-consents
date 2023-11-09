ALTER TABLE application_assets
    ADD COLUMN asset_type        TEXT,
    ADD COLUMN asset_id          INTEGER,
    ADD COLUMN cached_asset_name TEXT;

UPDATE application_assets
SET asset_type = 'FIELD',
    asset_id = field_id,
    cached_asset_name = cached_field_name
WHERE field_id IS NOT NULL;

UPDATE application_assets
SET asset_type = 'TERMINAL',
    asset_id = terminal_id,
    cached_asset_name = cached_terminal_name
WHERE terminal_id IS NOT NULL;

ALTER TABLE application_assets
    DROP COLUMN field_id,
    DROP COLUMN cached_field_name,
    DROP COLUMN terminal_id,
    DROP COLUMN cached_terminal_name;
