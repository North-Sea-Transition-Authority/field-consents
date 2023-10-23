ALTER TABLE application_consultation_further_information
    ADD COLUMN responded_at_datetime TIMESTAMPTZ,
    ADD COLUMN responded_by_wua_id   INT,
    ADD COLUMN response_text         TEXT
