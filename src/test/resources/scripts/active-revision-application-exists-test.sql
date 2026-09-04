-- Fixtures for ApplicationRepositoryIntegrationTest.
-- Each application_no is a consented application at variation 0, optionally followed by a revision at variation 1
-- whose version statuses determine whether the revision still counts as active.
INSERT INTO fcs.applications (id, type, created_date, created_by_wua_id, variation_no, application_no)
VALUES
  -- 9000: consented application with no revision
  (20000, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9000),
  -- 9001: revision still submitted
  (20001, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9001),
  (20002, 'PRODUCTION', '2026-03-11 17:09:44.368965 +00:00', 12345, 1, 9001),
  -- 9002: revision withdrawn
  (20003, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9002),
  (20004, 'PRODUCTION', '2026-03-11 17:09:44.368965 +00:00', 12345, 1, 9002),
  -- 9003: revision closed
  (20005, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9003),
  (20006, 'PRODUCTION', '2026-03-11 17:09:44.368965 +00:00', 12345, 1, 9003),
  -- 9004: revision deleted before it was ever submitted
  (20007, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9004),
  (20008, 'PRODUCTION', '2026-03-11 17:09:44.368965 +00:00', 12345, 1, 9004),
  -- 9005: submitted revision with a deleted draft update on top
  (20009, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9005),
  (20010, 'PRODUCTION', '2026-03-11 17:09:44.368965 +00:00', 12345, 1, 9005),
  -- 9006: closed revision with a deleted draft update on top
  (20011, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 9006),
  (20012, 'PRODUCTION', '2026-03-11 17:09:44.368965 +00:00', 12345, 1, 9006);

INSERT INTO fcs.application_versions (id, application_id, version_no, primary_operator_ou_id,
                                      cached_primary_operator_name, status, created_date_time, created_by_wua_id,
                                      submitted_date_time, submitted_by_wua_id, case_officer_wua_id, cam_wua_id,
                                      current_case_owner, migrated, auto_submitted_by_wua_id)
VALUES
  -- 9000: consented application. Nothing sits above it, so the revision join finds no rows at all.
  (30000, 20000, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),

  -- 9001: the consent being revised.
  (30001, 20001, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  -- 9001 revision: still submitted, so it is live and correctly blocks a further revision.
  (30002, 20002, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'SUBMITTED',
   '2026-03-11 17:09:44.373768 +00:00', 12345, '2026-03-11 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),

  -- 9002: the consent being revised.
  (30003, 20003, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  -- 9002 revision: withdrawn, so the WHERE clause excludes it and the consent is revisable again.
  (30004, 20004, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'WITHDRAWN',
   '2026-03-11 17:09:44.373768 +00:00', 12345, '2026-03-11 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),

  -- 9003: the consent being revised.
  (30005, 20005, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  -- 9003 revision: EDGE CASE, and the regression this fixture exists for. CLOSED was added to the status enum
  -- after this query was written and was never added to its exclusions, so a consent whose revision had been
  -- closed could never be revised again. CLOSED is terminal in the same way WITHDRAWN is.
  (30006, 20006, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CLOSED',
   '2026-03-11 17:09:44.373768 +00:00', 12345, '2026-03-11 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),

  -- 9004: the consent being revised.
  (30007, 20007, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  -- 9004 revision: EDGE CASE. Its only version is deleted, so the MAX(version_no) subquery (which skips
  -- DELETED) returns null, the join matches nothing, and the revision is treated as never having existed.
  (30008, 20008, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'DELETED',
   '2026-03-11 17:09:44.373768 +00:00', 12345, null, null,
   null, null, null, false, null),

  -- 9005: the consent being revised.
  (30009, 20009, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  -- 9005 revision: EDGE CASE. Deleting a draft update reverts the application to the version beneath it, so
  -- the subquery steps back past the DELETED version 2 to the SUBMITTED version 1. The revision is still live.
  (30010, 20010, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'SUBMITTED',
   '2026-03-11 17:09:44.373768 +00:00', 12345, '2026-03-11 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  (30011, 20010, 2, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'DELETED',
   '2026-03-12 17:09:44.373768 +00:00', 12345, null, null,
   null, null, null, false, null),

  -- 9006: the consent being revised.
  (30012, 20011, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  -- 9006 revision: EDGE CASE. Both of the above at once. The subquery steps back past the DELETED version 2,
  -- and the CLOSED version 1 it lands on is terminal, so the consent is revisable again.
  (30013, 20012, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CLOSED',
   '2026-03-11 17:09:44.373768 +00:00', 12345, '2026-03-11 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null),
  (30014, 20012, 2, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'DELETED',
   '2026-03-12 17:09:44.373768 +00:00', 12345, null, null,
   null, null, null, false, null);
