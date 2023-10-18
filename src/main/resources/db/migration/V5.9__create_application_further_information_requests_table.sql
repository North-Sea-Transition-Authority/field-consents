CREATE TABLE application_consultation_further_information_requests (
    id                    SERIAL PRIMARY KEY,
    consultation_id       INT         NOT NULL REFERENCES application_consultations(id),
    requested_at_datetime TIMESTAMPTZ NOT NULL,
    requested_by_wua_id   INT         NOT NULL,
    request_text          TEXT        NOT NULL,
    status                TEXT        NOT NULL
);

CREATE INDEX application_consultation_firs_consultation_idx
    ON application_consultation_further_information_requests(consultation_id);
