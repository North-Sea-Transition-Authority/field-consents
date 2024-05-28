ALTER TABLE application_assets
  ALTER COLUMN cached_asset_operator_name SET NOT NULL,
  ALTER COLUMN asset_type SET NOT NULL,
  ALTER COLUMN asset_id SET NOT NULL,
  ALTER COLUMN cached_asset_name SET NOT NULL;
