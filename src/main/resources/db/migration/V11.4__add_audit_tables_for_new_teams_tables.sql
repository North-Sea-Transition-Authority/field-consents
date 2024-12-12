CREATE TABLE teams_aud (
    rev        SERIAL,
    revtype    NUMERIC,
    id         UUID,
    type       TEXT,
    name       TEXT,
    scope_type TEXT,
    scope_id   TEXT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX teams_aud_rev_idx ON teams_aud(rev);

CREATE TABLE team_roles_aud (
    rev     SERIAL,
    revtype NUMERIC,
    id      UUID,
    team_id UUID,
    role    TEXT,
    wua_id  BIGINT,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES audit_revisions(rev)
);

CREATE INDEX team_roles_aud_rev_idx ON teams_aud(rev);