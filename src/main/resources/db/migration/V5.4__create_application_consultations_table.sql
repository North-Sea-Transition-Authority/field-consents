CREATE TABLE application_consultations (
    id                             SERIAL PRIMARY KEY,
    request_application_version_id INT       NOT NULL REFERENCES application_versions(id),
    consultation_team_id           INT       NOT NULL REFERENCES teams(id),
    request_deadline               TIMESTAMP NOT NULL,
    requested_at_datetime          TIMESTAMP NOT NULL,
    requested_by_wua_id            INT       NOT NULL
);

CREATE INDEX application_consultations_request_application_version_idx
    ON application_consultations(request_application_version_id);

CREATE INDEX application_consultations_consultation_team_idx
    ON application_consultations(consultation_team_id);
