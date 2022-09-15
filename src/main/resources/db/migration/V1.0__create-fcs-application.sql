CREATE TABLE IF NOT EXISTS applications (
    id                  SERIAL PRIMARY KEY,
    type                TEXT NOT NULL,
    created_date        TIMESTAMP NOT NULL,
    created_by_wua_id   INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS application_versions (
    id              SERIAL PRIMARY KEY,
    application_id  INTEGER NOT NULL,
    version_no      INTEGER NOT NULL,
    CONSTRAINT app_versions_fk1_app_id
    FOREIGN KEY (application_id)
        REFERENCES applications (id)
);

CREATE INDEX app_versions_idx1_app_id ON application_versions(application_id);