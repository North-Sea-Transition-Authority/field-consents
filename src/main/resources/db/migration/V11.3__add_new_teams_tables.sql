CREATE TABLE teams (
    id         UUID PRIMARY KEY,
    type       TEXT NOT NULL,
    name       TEXT NOT NULL,
    scope_type TEXT,
    scope_id   TEXT
);

-- If the team is scoped, it's type+scopeType+scopeId must be unique
CREATE UNIQUE INDEX teams_scoped_unique ON teams(type, scope_type, scope_id) WHERE (scope_type IS NOT NULL);

-- If the teams is not scoped, it's type must be unique
CREATE UNIQUE INDEX teams_static_unique ON teams(type) WHERE (scope_type IS NULL);

CREATE TABLE team_roles (
    id      UUID PRIMARY KEY,
    team_id UUID   NOT NULL REFERENCES teams(id),
    role    TEXT   NOT NULL,
    wua_id  BIGINT NOT NULL
);

CREATE INDEX team_roles_team_id_idx ON team_roles(team_id);