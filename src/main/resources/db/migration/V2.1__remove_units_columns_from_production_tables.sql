ALTER TABLE annual_production_months
DROP COLUMN oil_min_unit,
DROP COLUMN oil_max_unit,
DROP COLUMN gas_min_unit,
DROP COLUMN gas_max_unit;

ALTER TABLE short_term_production_months
DROP COLUMN oil_min_unit,
DROP COLUMN oil_max_unit,
DROP COLUMN gas_min_unit,
DROP COLUMN gas_max_unit;

ALTER TABLE long_term_production_years
DROP COLUMN oil_min_unit,
DROP COLUMN oil_max_unit,
DROP COLUMN gas_min_unit,
DROP COLUMN gas_max_unit;