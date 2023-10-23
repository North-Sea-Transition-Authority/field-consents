
--DELETE FROM fcs_migration.consent_lengths;
--DELETE FROM fcs_migration.application_versions;
--DELETE FROM fcs_migration.applications;


--
-- applications
--

INSERT INTO fcs_migration.applications(
  id
, type
, created_date
, created_by_wua_id
, variation_no
, application_no
, fc_id
)
SELECT
--  CASE
--  WHEN fcd.variation_no = 0 THEN fcd.fc_id
--  ELSE fcs_migration.application_id_seq.nextval
--  END
  fcs_migration.application_id_seq.nextval
, CASE fcd.application_type
  WHEN 'PCON' THEN 'PRODUCTION'
  WHEN 'FCON' THEN 'FLARE'
  WHEN 'VCON' THEN 'VENT'
  END type
, fcd.created_date -- the initial create date, i.e. the first detail row per variation set
, fcd.created_by created_by_wua_id
, fcd.variation_no
, fcd.fc_id application_no
, fcd.fc_id
FROM envmgr.field_consents fc
JOIN envmgr.field_consent_details fcd ON fcd.fc_id = fc.id
-- In the new world, we need a row per application, per variation. So, here we choose the earliest
-- fcd for each variation in the old world and use that as our master fc in the new world
WHERE fcd.id = (
  SELECT min(ifcd.id)
  FROM envmgr.field_consent_details ifcd
  WHERE ifcd.fc_id = fcd.fc_id
  AND ifcd.variation_no = fcd.variation_no
);
/

--
-- application_versions
--
-- migration rules:
-- Don't migrate:
-- 1) CANCELLED, CURRENT, null version/variation, no case ref, no master fc_id - detail rows for applications that were never submitted and were deleted (Cancel link in the workbasket)
-- 2) INPROGRESS, CURRENT, version 1, no case ref, no master fc_id - detail rows for applications that were never submitted 
-- 3) INPROGRESS, PENDING, any version - detail row for in progress app updates
INSERT INTO fcs_migration.application_versions (
  id
, application_id
, version_no
, primary_operator_ou_id
, cached_primary_operator_name
, status
, created_date_time
, created_by_wua_id
, submitted_date_time
, submitted_by_wua_id
, case_officer_wua_id
)
SELECT
  fcd.id -- this is using the fcd_id as the app version id
, ap.id application_no
, fcd.version_no
, xfcd.operator_ou_id primary_operator_ou_id
, ou.name cached_primary_operator_name
, CASE
  -- CANCELLED PENDING - a cancelled version update that was never submitted (for any variation)
  WHEN fcd.status = 'CANCELLED' AND fcd.version_status = 'PENDING' THEN 'DELETED'
  -- CANCELLED CURRENT
  --  - if fc_id empty     => a cancelled initial version that was never submitted (don't migrate)
  --  - if fc_id non-empty => a cancelled version that was submitted and then withdrawn (migrate to WITHDRAWN)
  WHEN fcd.status = 'CANCELLED' AND fcd.version_status = 'CURRENT' THEN 'WITHDRAWN'
  -- RECEIVEDBYBERR ARCHIVED - a submitted version that has been superceeded
  -- RECEIVEDBYBERR CURRENT - the tip submitted version
  WHEN fcd.status = 'RECEIVEDBYBERR' THEN 'SUBMITTED'
  -- COMPLETED CURRENT - the tip version row - consented
  WHEN fcd.status = 'COMPLETED' THEN 'COMPLETED'
  END status
, fcd.created_date created_date_time
, fcd.created_by created_by_wua_id
, fcd.submitted_date submitted_date_time
, fcd.submitted_by submitted_by_wua_id
, NULL case_officer_wua_id -- TODO?
FROM envmgr.field_consent_details fcd
JOIN fcs_migration.applications ap ON ap.fc_id = fcd.fc_id AND ap.variation_no = fcd.variation_no  
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
JOIN decmgr.xview_organisation_units ou ON ou.organ_id = xfcd.operator_ou_id
WHERE (fcd.status, fcd.version_status) NOT IN (
  ('INPROGRESS', 'PENDING') -- an unsubmitted application update (for any version/variation) (don't migrate)
, ('INPROGRESS', 'CURRENT') -- an unsubmitted application (version 1 variation 0) (don't migrate)
)
AND fcd.fc_id IS NOT NULL; -- implies never submitted (don't migrate)
/

--
-- consent_lengths
--

INSERT INTO fcs_migration.consent_lengths (
  id
, application_version_id
, consent_length
, annual_consent_year
, short_term_start_date
, short_term_end_date
, long_term_start_year
, long_term_end_year
)
SELECT
  fcs_migration.consent_length_id_seq.nextval
, xfcd.fcd_id application_version_id
, xfcd.app_length consent_length
, CASE xfcd.app_length
  WHEN 'ANNUAL' THEN xfcd.application_year
  END annual_consent_year
, CASE xfcd.app_length
  WHEN 'SHORT_TERM' THEN to_date(cl.short_term_start_date, 'YYYY-MM-DD')
  END short_term_start_date
, CASE xfcd.app_length
  WHEN 'SHORT_TERM' THEN to_date(cl.short_term_end_date, 'YYYY-MM-DD')
  END short_term_end_date 
, CASE xfcd.app_length
  WHEN 'LONG_TERM' THEN
    CASE xfcd.application_type
    WHEN 'PCON' THEN cl.prod_long_term_start_year
    ELSE cl.fv_long_term_start_year
    END
  END long_term_start_year  
, CASE xfcd.app_length
  WHEN 'LONG_TERM' THEN
    CASE xfcd.application_type
    WHEN 'PCON' THEN cl.prod_long_term_end_year
    ELSE cl.fv_long_term_end_year
    END
  END long_term_end_year
FROM envmgr.xview_field_consent_details xfcd
JOIN envmgr.field_consent_details fcd ON fcd.id = xfcd.fcd_id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    short_term_start_date VARCHAR2(4000) PATH 'COVER_INFO/STC_START_DATE/text()'
  , short_term_end_date VARCHAR2(4000) PATH 'COVER_INFO/STC_END_DATE/text()'
  , prod_long_term_start_year NUMBER PATH 'LONG_TERM_PRODUCTION/PRODUCTION_DATA_LIST/PRODUCTION_DATA[1]/YEAR/text()'
  , prod_long_term_end_year NUMBER PATH 'LONG_TERM_PRODUCTION/PRODUCTION_DATA_LIST/PRODUCTION_DATA[last()]/YEAR/text()'
  , fv_long_term_start_year NUMBER PATH 'LONG_TERM_CONSENT/DATA_LIST/DATA[1]/YEAR/text()'
  , fv_long_term_end_year NUMBER PATH 'LONG_TERM_CONSENT/DATA_LIST/DATA[last()]/YEAR/text()'
) cl
WHERE EXISTS (
  SELECT 1
  FROM fcs_migration.application_versions av
  WHERE av.id = xfcd.fcd_id
);
