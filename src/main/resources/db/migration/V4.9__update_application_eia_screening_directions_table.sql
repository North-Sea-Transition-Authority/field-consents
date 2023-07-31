ALTER TABLE application_eia_directions
    ADD COLUMN for_purpose_of_eia_regs BOOLEAN;

ALTER TABLE application_eia_directions
    ALTER have_submitted_eia_direction DROP NOT NULL;
