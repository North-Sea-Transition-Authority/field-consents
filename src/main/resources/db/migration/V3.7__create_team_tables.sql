CREATE TABLE teams (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, type VARCHAR NOT NULL
, display_name TEXT
);

CREATE TABLE team_member_roles (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, wua_id INT NOT NULL
, team_id INT NOT NULL REFERENCES teams(id)
, role TEXT NOT NULL
);

CREATE INDEX team_member_roles_idx1_team_id ON team_member_roles(team_id);

INSERT INTO teams (type)
VALUES ('REGULATOR');