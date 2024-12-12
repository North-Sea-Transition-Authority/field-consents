ALTER TABLE application_consultations RENAME COLUMN consultation_team_id TO old_consultation_team_id;
ALTER TABLE application_consultations_aud RENAME COLUMN consultation_team_id TO old_consultation_team_id;

ALTER TABLE application_consultations ADD COLUMN consultation_team_id UUID REFERENCES teams(id);
ALTER TABLE application_consultations_aud ADD COLUMN consultation_team_id UUID;

UPDATE application_consultations c
SET consultation_team_id = (
    SELECT t.id
    FROM teams t
    WHERE t.old_team_id = c.old_consultation_team_id
);

UPDATE application_consultations_aud ca
SET consultation_team_id = (
    SELECT t.id
    FROM teams t
    WHERE t.old_team_id = ca.old_consultation_team_id
);

ALTER TABLE application_consultations ALTER COLUMN consultation_team_id SET NOT NULL;

ALTER TABLE application_consultations DROP COLUMN old_consultation_team_id;
ALTER TABLE application_consultations_aud DROP old_consultation_team_id;