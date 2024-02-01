ALTER TABLE application_units
    ADD COLUMN emission_category_type TEXT;

UPDATE application_units
SET emission_category_type = 'CATEGORY_ABC'
WHERE coalesce(flare_category_unit, vent_category_unit) IS NOT NULL;
