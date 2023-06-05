CREATE TABLE application_flags_aud (
    rev                    SERIAL
  , revtype                NUMERIC
  , id                     INT
  , application_version_id INT
  , flag_type              TEXT
  , flag_value             BOOLEAN
  , PRIMARY KEY (rev, id)
  , FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX idx_app_flags_aud_rev ON application_flags_aud(rev);
