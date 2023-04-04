-- Create stub user
INSERT INTO fcs.team_member_roles(team_id, wua_id, role)
VALUES (
  (
    WITH iterable_teams AS (
      SELECT
        row_number() OVER () row
      , t.*
      FROM fcs.teams t
    ), max_reg_team AS (
      SELECT MAX(it.row), it.id
      FROM iterable_teams it
      WHERE it.type = 'REGULATOR'
      GROUP BY it.id
    )
    SELECT mrt.id
    FROM max_reg_team mrt
  )
, :user_id
, :role
);