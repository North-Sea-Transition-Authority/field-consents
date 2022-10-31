CREATE TABLE IF NOT EXISTS vents (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    vent_no                INTEGER NOT NULL,
    vent_type              TEXT NOT NULL,
    description            TEXT,
    metered_flag           BOOLEAN NOT NULL,
    comments               TEXT,
    CONSTRAINT vents_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vents_idx1_av_id ON vents(application_version_id);