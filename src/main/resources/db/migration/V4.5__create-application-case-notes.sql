CREATE TABLE IF NOT EXISTS application_case_notes (
    id                      SERIAL PRIMARY KEY,
    application_version_id  INTEGER NOT NULL,
    added_by_wua_id         INTEGER NOT NULL,
    added_date_time         TIMESTAMP NOT NULL,
    case_note_text          TEXT NOT NULL,

    CONSTRAINT app_case_notes_fk1_version_id
        FOREIGN KEY (application_version_id)
            REFERENCES application_versions (id)
);

CREATE INDEX app_case_notes_idx1_version_id ON application_case_notes(application_version_id);
