ALTER TABLE flare_report_gas_data
    ALTER COLUMN category_a_density          DROP NOT NULL,
    ALTER COLUMN category_a_inert_percentage DROP NOT NULL,
    ALTER COLUMN category_a_hydro_percentage DROP NOT NULL,
    ALTER COLUMN category_b_density          DROP NOT NULL,
    ALTER COLUMN category_b_inert_percentage DROP NOT NULL,
    ALTER COLUMN category_b_hydro_percentage DROP NOT NULL,
    ALTER COLUMN category_c_density          DROP NOT NULL,
    ALTER COLUMN category_c_inert_percentage DROP NOT NULL,
    ALTER COLUMN category_c_hydro_percentage DROP NOT NULL;

ALTER TABLE vent_report_gas_data
    ALTER COLUMN category_a_density          DROP NOT NULL,
    ALTER COLUMN category_a_inert_percentage DROP NOT NULL,
    ALTER COLUMN category_a_hydro_percentage DROP NOT NULL,
    ALTER COLUMN category_b_density          DROP NOT NULL,
    ALTER COLUMN category_b_inert_percentage DROP NOT NULL,
    ALTER COLUMN category_b_hydro_percentage DROP NOT NULL,
    ALTER COLUMN category_c_density          DROP NOT NULL,
    ALTER COLUMN category_c_inert_percentage DROP NOT NULL,
    ALTER COLUMN category_c_hydro_percentage DROP NOT NULL;
