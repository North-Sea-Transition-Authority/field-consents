ALTER TABLE teams ADD COLUMN old_team_id INTEGER;

INSERT INTO teams (id, old_team_id, type, name, scope_type, scope_id)
SELECT
    gen_random_uuid(),
    id,
    type,
    CASE WHEN display_name IS NULL THEN type ELSE display_name END,
    'ORGANISATION_GROUP_ID',
    organisation_group_id
FROM teams_old
WHERE type = 'INDUSTRY';

INSERT INTO teams (id, old_team_id, type, name)
SELECT
    gen_random_uuid(),
    id,
    type,
    CASE WHEN display_name IS NULL THEN type ELSE display_name END
FROM teams_old
WHERE type != 'INDUSTRY';

INSERT INTO team_roles (id, team_id, role, wua_id)
SELECT
    gen_random_uuid(),
    t.id,
    tmro.role,
    tmro.wua_id
FROM team_member_roles_old tmro
JOIN teams t ON t.old_team_id = tmro.team_id;

UPDATE teams
SET name = 'NSTA'
WHERE type = 'REGULATOR'