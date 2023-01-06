CREATE TABLE IF NOT EXISTS application_assets (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,    
    field_id                INTEGER,
    cached_field_name       TEXT,
    terminal_id             INTEGER,
    cached_terminal_name    TEXT,
    asset_role              TEXT NOT NULL,
    CONSTRAINT app_assets_fk1_version_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
    );

CREATE INDEX app_assets_idx1_version_id ON application_assets(application_version_id);