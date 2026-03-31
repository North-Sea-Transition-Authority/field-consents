INSERT INTO fcs.applications (id, type, created_date, created_by_wua_id, variation_no, application_no)
VALUES
  (10001, 'FLARE', '2026-03-19 17:09:44.368965 +00:00', 38138, 0, 8000),
  (10002, 'FLARE', '2026-03-19 17:09:44.368965 +00:00', 38138, 0, 8001),
  (10003, 'FLARE', '2026-03-19 17:09:44.368965 +00:00', 38138, 0, 8002),
  (10004, 'FLARE', '2026-03-19 17:09:44.368965 +00:00', 38138, 0, 8002);


INSERT INTO fcs.application_versions (id, application_id, version_no, primary_operator_ou_id,
                                      cached_primary_operator_name, status, created_date_time, created_by_wua_id,
                                      submitted_date_time, submitted_by_wua_id, case_officer_wua_id, cam_wua_id,
                                      current_case_owner, migrated, auto_submitted_by_wua_id)
VALUES
  (10001, 10001, 1, 304,
   'BP EXPLORATION (ALPHA) LIMITED', 'SUBMITTED',
   '2026-03-19 17:09:44.373768 +00:00', 38138,
   '2026-03-19 17:22:13.595215 +00:00', 38138,
   38137, null, 'CASE_OFFICER', false, null),
  (10002, 10002, 1, 20,
   'BP EXPLORATION OPERATING COMPANY LIMITED', 'SUBMITTED',
   '2026-03-19 17:09:44.373768 +00:00', 38138,
   '2026-03-19 17:22:13.595215 +00:00', 38138,
   38137, null, 'CASE_OFFICER', false, null),
  (10003, 10002, 2, 12,
   'SHELL U.K. LIMITED', 'WITHDRAWN',
   '2026-03-19 17:09:44.373768 +00:00', 38138,
   '2026-03-19 17:22:13.595215 +00:00', 38138,
   38137, null, 'CASE_OFFICER', false, null),
  (10004, 10003, 1, 12,
   'SHELL U.K. LIMITED', 'SUBMITTED',
   '2026-03-19 17:09:44.373768 +00:00', 38138,
   '2026-03-19 17:22:13.595215 +00:00', 38138,
   38137, null, 'CASE_OFFICER', false, null),
  (10005, 10004, 1, 20,
   'BP EXPLORATION OPERATING COMPANY LIMITED', 'SUBMITTED',
   '2026-03-19 17:09:44.373768 +00:00', 38138,
   '2026-03-19 17:22:13.595215 +00:00', 38138,
   38137, null, 'CASE_OFFICER', false, null),
  (10006, 10004, 2, 12,
   'SHELL U.K. LIMITED', 'DELETED',
   '2026-03-19 17:09:44.373768 +00:00', 38138,
   '2026-03-19 17:22:13.595215 +00:00', 38138,
   38137, null, 'CASE_OFFICER', false, null);
