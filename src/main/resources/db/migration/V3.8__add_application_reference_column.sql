ALTER TABLE applications
ADD COLUMN variation_no INT,
ADD COLUMN application_no INT;

UPDATE applications
SET variation_no = 0
WHERE variation_no IS NULL;

ALTER TABLE applications
ALTER COLUMN variation_no SET NOT NULL;

ALTER TABLE applications
ADD CONSTRAINT app_number_unique UNIQUE (application_no);