CREATE TABLE IF NOT EXISTS flares (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    flare_no               INTEGER NOT NULL,
    flare_type             TEXT NOT NULL,
    description            TEXT,
    metered_flag           BOOLEAN NOT NULL,
    comments               TEXT,
    CONSTRAINT flares_fk1_av_id
    FOREIGN KEY (application_version_id)
        REFERENCES application_versions (id)
);

CREATE INDEX flares_idx1_av_id ON flares(application_version_id);
