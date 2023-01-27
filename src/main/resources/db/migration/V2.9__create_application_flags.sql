CREATE TABLE IF NOT EXISTS application_flags (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    flag_type               TEXT NOT NULL,
    flag_value              BOOLEAN NOT NULL,
    UNIQUE (application_version_id, flag_type),
    CONSTRAINT app_flags_fk1_version_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX app_flags_idx1_version_id ON application_flags(application_version_id);