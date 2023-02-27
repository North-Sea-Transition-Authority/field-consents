CREATE TABLE IF NOT EXISTS application_eia_directions (
    id                              SERIAL PRIMARY KEY,
    application_version_id          INTEGER NOT NULL,
    have_submitted_eia_direction    BOOLEAN NOT NULL,
    sat_id                          INTEGER,
    cached_sat_ref                  TEXT,
    have_eia_direction_to_submit    BOOLEAN,
    latest_date_to_be_submitted     DATE,
    why_no_eia_direction            TEXT,
    CONSTRAINT application_eia_directions_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX application_eia_directions_idx1_av_id ON application_eia_directions(application_version_id);
