ALTER TABLE applications
    DROP CONSTRAINT app_number_unique;

ALTER TABLE applications
    ADD CONSTRAINT variation_no_application_no_unique UNIQUE (variation_no, application_no);
