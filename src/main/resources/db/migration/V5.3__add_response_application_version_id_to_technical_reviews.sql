ALTER TABLE application_technical_reviews
    ADD COLUMN response_application_version_id INT REFERENCES application_versions(id);

CREATE INDEX application_technical_reviews_response_application_version_idx
    ON application_technical_reviews(response_application_version_id);

UPDATE application_technical_reviews
SET response_application_version_id = application_version_id
WHERE response_application_version_id IS NULL AND technical_review_status = 'CLOSED';

ALTER TABLE application_technical_reviews
    RENAME COLUMN application_version_id TO request_application_version_id;

ALTER TABLE application_technical_reviews_aud
    ADD COLUMN response_application_version_id INT;

ALTER TABLE application_technical_reviews_aud
    RENAME COLUMN application_version_id TO request_application_version_id;
