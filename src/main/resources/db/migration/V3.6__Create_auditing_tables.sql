CREATE TABLE audit_revisions (
    rev                 SERIAL PRIMARY KEY
  , created_date_time   TIMESTAMP
  , user_wua_id         INT
);

CREATE TABLE application_versions_aud (
    rev                   SERIAL
  , revtype               NUMERIC
  , id                    INT
  , status                TEXT
  , PRIMARY KEY (rev, id)
  , FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_app_versions_aud_rev ON application_versions_aud(rev);