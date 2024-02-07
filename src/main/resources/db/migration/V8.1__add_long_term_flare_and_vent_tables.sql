--
-- flare
--
CREATE TABLE IF NOT EXISTS flare_long_term_years (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    gas                    NUMERIC NOT NULL,
    CONSTRAINT flare_long_term_years_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX flare_long_term_years_idx1_av_id ON flare_long_term_years(application_version_id);

--
-- vent
--
CREATE TABLE IF NOT EXISTS vent_long_term_years (
    id                     SERIAL PRIMARY KEY,
    application_version_id INTEGER NOT NULL,
    year                   INTEGER NOT NULL,
    gas                    NUMERIC NOT NULL,
    CONSTRAINT vent_long_term_years_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX vent_long_term_years_idx1_av_id ON vent_long_term_years(application_version_id);
