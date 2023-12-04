ALTER TABLE flares
    ALTER COLUMN metered_flag DROP NOT NULL;

ALTER TABLE vents
    ALTER COLUMN metered_flag DROP NOT NULL;

ALTER TABLE application_updates
    ALTER COLUMN deadline_date_time DROP NOT NULL;
