ALTER TABLE application_supporting_information
    ALTER COLUMN notes DROP NOT NULL;

ALTER TABLE long_term_production_years
    ALTER COLUMN oil_min_value DROP NOT NULL,
    ALTER COLUMN oil_max_value DROP NOT NULL,
    ALTER COLUMN gas_min_value DROP NOT NULL,
    ALTER COLUMN gas_max_value DROP NOT NULL;

ALTER TABLE annual_production_months
    ALTER COLUMN oil_min_value DROP NOT NULL,
    ALTER COLUMN gas_min_value DROP NOT NULL;

ALTER TABLE flare_report_gas_data
    ALTER COLUMN evaluated_per_category DROP NOT NULL;

ALTER TABLE vent_report_gas_data
    ALTER COLUMN evaluated_per_category DROP NOT NULL;
