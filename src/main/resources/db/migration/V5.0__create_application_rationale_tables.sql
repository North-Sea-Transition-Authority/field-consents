CREATE TABLE application_rationale (
    id                     SERIAL PRIMARY KEY,
    application_version_id INT  NOT NULL REFERENCES application_versions(id),
    rationale_type         TEXT NOT NULL,
    comment                TEXT
);

CREATE INDEX application_rationale_application_version_idx ON application_rationale(application_version_id);
