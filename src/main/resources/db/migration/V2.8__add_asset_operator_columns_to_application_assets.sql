ALTER TABLE application_assets
ADD COLUMN asset_no INTEGER,
ADD COLUMN asset_operator_ou_id INTEGER,
ADD COLUMN cached_asset_operator_name TEXT;

UPDATE application_assets
SET asset_operator_ou_id = 0;

ALTER TABLE application_assets
ALTER COLUMN asset_operator_ou_id SET NOT NULL;