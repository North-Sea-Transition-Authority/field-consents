CREATE TABLE IF NOT EXISTS application_supporting_information (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    notes                   TEXT,
    erap_notes              TEXT,
    CONSTRAINT application_supporting_information_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX application_supporting_information_idx1_av_id ON application_supporting_information(application_version_id);
