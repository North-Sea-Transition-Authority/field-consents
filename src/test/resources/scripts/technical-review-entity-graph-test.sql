-- Fixtures for TechnicalReviewRepositoryIntegrationTest.
-- One application with a known application_no, one version, and two technical reviews for the same reviewer:
-- an OPEN one (only a request version) and a CLOSED one (request + response version). The tests fetch these via
-- the "technicalReview" entity graph OUTSIDE a transaction, then read the nested application_no; the graph must
-- eagerly load requestApplicationVersion.application and responseApplicationVersion.application or that read
-- throws LazyInitializationException.
INSERT INTO fcs.applications (id, type, created_date, created_by_wua_id, variation_no, application_no)
VALUES
  (40000, 'PRODUCTION', '2026-03-10 17:09:44.368965 +00:00', 12345, 0, 700);

INSERT INTO fcs.application_versions (id, application_id, version_no, primary_operator_ou_id,
                                      cached_primary_operator_name, status, created_date_time, created_by_wua_id,
                                      submitted_date_time, submitted_by_wua_id, case_officer_wua_id, cam_wua_id,
                                      current_case_owner, migrated, auto_submitted_by_wua_id)
VALUES
  (41000, 40000, 1, 304, 'BP EXPLORATION (ALPHA) LIMITED', 'CONSENTED',
   '2026-03-10 17:09:44.373768 +00:00', 12345, '2026-03-10 17:22:13.595215 +00:00', 12345,
   null, null, null, false, null);

INSERT INTO fcs.application_technical_reviews (id, request_application_version_id, requested_by_wua_id,
                                               requested_date_time, request_text, deadline_date_time,
                                               technical_reviewer_wua_id, responded_by_wua_id, responded_date_time,
                                               response_text, response_type, technical_review_status,
                                               response_application_version_id)
VALUES
  -- OPEN review: only a request version.
  (42000, 41000, 12345, '2026-03-10 17:09:44.373768 +00:00', 'Please review', '2026-03-12 17:09:44.373768 +00:00',
   98765, null, null, null, null, 'OPEN', null),
  -- CLOSED review: request and response version both set.
  (42001, 41000, 12345, '2026-03-10 17:09:44.373768 +00:00', 'Please review', '2026-03-12 17:09:44.373768 +00:00',
   98765, 98765, '2026-03-11 17:09:44.373768 +00:00', 'Approved', 'APPROVE', 'CLOSED', 41000);
