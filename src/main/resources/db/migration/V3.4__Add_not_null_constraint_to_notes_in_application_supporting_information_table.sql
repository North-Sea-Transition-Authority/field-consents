UPDATE application_supporting_information
SET notes = '';

ALTER TABLE application_supporting_information
ALTER COLUMN notes SET NOT NULL;