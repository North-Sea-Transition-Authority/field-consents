ALTER TABLE application_consultations
    ADD COLUMN response_application_version_id    INT REFERENCES application_versions(id),
    ADD COLUMN responded_at_datetime              TIMESTAMPTZ,
    ADD COLUMN responded_by_wua_id                INT,
    ADD COLUMN habitats_regs_response_type        TEXT,
    ADD COLUMN habitats_regs_response_description TEXT,
    ADD COLUMN eia_regs_response_type             TEXT,
    ADD COLUMN eia_regs_response_description      TEXT;

CREATE INDEX application_consultations_response_application_version_idx
    ON application_consultations(response_application_version_id);

ALTER TABLE application_consultations_aud
    ADD COLUMN response_application_version_id    INT,
    ADD COLUMN responded_at_datetime              TIMESTAMPTZ,
    ADD COLUMN responded_by_wua_id                INT,
    ADD COLUMN habitats_regs_response_type        TEXT,
    ADD COLUMN habitats_regs_response_description TEXT,
    ADD COLUMN eia_regs_response_type             TEXT,
    ADD COLUMN eia_regs_response_description      TEXT;
