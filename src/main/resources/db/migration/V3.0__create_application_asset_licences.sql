CREATE TABLE IF NOT EXISTS application_asset_licences (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    application_asset_id    INTEGER NOT NULL,
    licence_id              INTEGER NOT NULL,
    cached_licence_ref      TEXT NOT NULL,
    CONSTRAINT app_asset_licences_fk1_av_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id),
    CONSTRAINT app_asset_licences_fk2_aa_id
        FOREIGN KEY (application_asset_id)
            REFERENCES application_assets (id)
);

CREATE INDEX app_asset_licences_idx1_av_id ON application_asset_licences(application_version_id);

CREATE INDEX app_asset_licences_idx2_aa_id ON application_asset_licences(application_asset_id);
