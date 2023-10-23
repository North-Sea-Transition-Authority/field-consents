
-- TODO
--
-- 1) We have Long Term Flare and Vent cases in the legacy system but not in the
--    new system. Data is being migrated but we have no long term flare/vent screens
--    to show the data. Also the new screen likely won't cope well with these cases.
--    This all needs screen testing before deciding what to do.

-- NOTES
-- 1) for a variation the fc_id stays the same (the variation no is on the detail row
--    and therefore we can have multiple details with the same version_no but different variation_no)
--    the ref number matches the fc_id in the legacy system
--    ? do we need to move the variation no from the applications table to the application versions table? NO
-- 


-- the ref_number can be constructed as below so no need to migrate as the part will be migrated
WITH t AS (
  SELECT fcd.ref_number , fcd.application_type||'/'||fcd.fc_id||'/'||fcd.variation_no||' (Version '||fcd.version_no||')' AS ref_number_constructed
  FROM envmgr.XVIEW_FIELD_CONSENT_DETAILS fcd
  WHERE fcd.status NOT IN ('CANCELLED')
  AND upper(fcd.ref_number) != 'NOT YET ASSIGNED'
)
SELECT t.*
FROM t
WHERE t.ref_number != t.ref_number_constructed
/

SELECT max(fcd.variation_no)
FROM envmgr.field_consent_details fcd
/

--
-- Checks
--
SELECT ap.*, av.*, cl.*
FROM applications ap
JOIN application_versions av ON av.application_id = ap.id
JOIN consent_lengths cl ON cl.application_version_id = av.id
--WHERE av.cached_primary_operator_name IS NULL
ORDER BY ap.application_no ASC, ap.id ASC, av.id ASC
/



--field_consent_details
-----------------------
--id
--fc_id
--application_type
--variation_no
--version_no
--status
--xml_data
--version_status
--created_by
--created_date
--last_updated_by
--last_updated_date
--submitted_by
--submitted_date


--  id INTEGER,
--  type VARCHAR2(4000) NOT NULL,
--  created_date      TIMESTAMP WITH TIME ZONE NOT NULL,
--  created_by_wua_id INTEGER NOT NULL,
--  variation_no      INTEGER NOT NULL,
--  application_no    INTEGER
--
-- applications
--
SELECT
  fc.id
, CASE fcd.application_type
  WHEN 'PCON' THEN 'PRODUCTION'
  WHEN 'FCON' THEN 'FLARE'
  WHEN 'VCON' THEN 'VENT'
  END type
, fcd.created_date -- the initial create date, i.e. the first detail row
, fcd.created_by created_by_wua_id
, fcd.variation_no 
, fcd.fc_id application_no
--, fcd.version_status
--, fcd.id fcd_id
FROM envmgr.field_consents fc
JOIN envmgr.field_consent_details fcd ON fcd.fc_id = fc.id
WHERE fcd.id = (
  SELECT min(ifcd.id)
  FROM envmgr.field_consent_details ifcd
  WHERE ifcd.fc_id = fcd.fc_id
  AND ifcd.variation_no = fcd.variation_no
) -- only pick the first detail per variation
ORDER BY fc.id ASC, fcd.id ASC
/
SELECT a.*
FROM fcs_migration.applications a
ORDER BY a.id ASC
/

--  id                           INTEGER PRIMARY KEY
--, application_id               INTEGER NOT NULL
--                               CONSTRAINT app_versions_fk1_app_id
--                               REFERENCES fcs_migration.applications
--, version_no                   INTEGER NOT NULL
--, primary_operator_ou_id       INTEGER NOT NULL
--, cached_primary_operator_name VARCHAR2(4000)
--, status                       VARCHAR2(4000) NOT NULL
--, created_date_time            DATE NOT NULL
--, created_by_wua_id            INTEGER NOT NULL
--, submitted_date_time          DATE
--, submitted_by_wua_id          INTEGER
--, case_officer_wua_id          INTEGER
--
-- application_versions
--
WITH t AS (
SELECT
  fcd.id
, fcd.fc_id
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
  WHEN fcd.status = 'CANCELLED' AND fcd.version_status = 'CURRENT' AND fcd.fc_id IS NOT NULL THEN 'WITHDRAWN'
  -- RECEIVEDBYBERR ARCHIVED - a submitted version that has been superceeded
  -- RECEIVEDBYBERR CURRENT - the tip submitted version
  WHEN fcd.status = 'RECEIVEDBYBERR' THEN 'SUBMITTED'
  -- COMPLETED CURRENT - the tip version row - consented
  WHEN fcd.status = 'COMPLETED' THEN 'COMPLETED'
  END new_status
, fcd.created_date created_date_time
, fcd.created_by created_by_wua_id
, fcd.submitted_date submitted_date_time
, fcd.submitted_by submitted_by_wua_id
, NULL case_officer_wua_id -- TODO
, fcd.status
, fcd.version_status
, fcd.variation_no
, xfcd.ref_number
--DISTINCT fcd.status, fcd.version_status
--, xfcd.*
--count(*)
FROM envmgr.field_consent_details fcd
JOIN fcs_migration.applications ap ON ap.fc_id = fcd.fc_id AND ap.variation_no = fcd.variation_no  
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
JOIN decmgr.xview_organisation_units ou ON ou.organ_id = xfcd.operator_ou_id
WHERE (fcd.status, fcd.version_status) NOT IN (
  ('INPROGRESS', 'PENDING') -- an unsubmitted application update (for any version/variation) (don't migrate)
, ('INPROGRESS', 'CURRENT') -- an unsubmitted application (version 1 variation 0) (don't migrate)
)
AND fcd.fc_id IS NOT NULL -- implies never submitted (don't migrate)
ORDER BY fcd.fc_id ASC, fcd.id ASC
)
SELECT
--DISTINCT t.new_status, t.status, t.version_status
t.*
FROM t
--WHERE t.new_status IN ('WITHDRAWN')
--OR t.new_status IS NULL
--AND t.application_id IS NULL
/
SELECT -- DISTINCT fcd.status, fcd.version_status
  fcd.id
, fcd.fc_id
, fcd.version_no
, fcd.created_date created_date_time
, fcd.created_by created_by_wua_id
, fcd.submitted_date submitted_date_time
, fcd.submitted_by submitted_by_wua_id
, fcd.status
, fcd.version_status
, fcd.variation_no
, xfcd.ref_number
FROM envmgr.field_consent_details fcd
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
--WHERE fcd.status = 'INPROGRESS'
--WHERE fcd.status = 'CANCELLED' AND fcd.version_status = 'PENDING' AND fcd.version_no > 1
--WHERE fcd.fc_id = 2036
--WHERE fcd.id = 4316
ORDER BY fcd.fc_id ASC, fcd.id ASC
--ORDER BY 1, 2
/

--4316FCD - this was a cancelled application before it was submitted


--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT app_types_fk1_version_id
--                         REFERENCES application_versions
--, consent_length         VARCHAR2(4000) NOT NULL
--, annual_consent_year    INTEGER
--, short_term_start_date  DATE
--, short_term_end_date    DATE
--, long_term_start_year   INTEGER
--, long_term_end_year     INTEGER
--
-- consent_lengths
--
WITH t AS (
SELECT
  null id
, xfcd.fcd_id
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
, xfcd.status
, xfcd.application_type
, cl.prod_long_term_start_year
, cl.prod_long_term_end_year
, cl.fv_long_term_start_year
, cl.fv_long_term_end_year
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
--LEFT JOIN XMLTABLE(
--  '/FIELD_CONSENT/*[name()=''LONG_TERM_PRODUCTION'' or name()=''LONG_TERM_CONSENT'']/*[name()=''PRODUCTION_DATA_LIST'' or name()=''DATA_LIST'']/*[name()=''PRODUCTION_DATA'' or name()=''DATA''][1]'
--  PASSING 
--    fcd.xml_data
--  COLUMNS
--    long_term_start_year NUMBER PATH 'YEAR/text()'
--) ltsy ON 1=1
--LEFT JOIN XMLTABLE(
--  '/FIELD_CONSENT/*[name()=''LONG_TERM_PRODUCTION'' or name()=''LONG_TERM_CONSENT'']/*[name()=''PRODUCTION_DATA_LIST'' or name()=''DATA_LIST'']/*[name()=''PRODUCTION_DATA'' or name()=''DATA''][last()]'
--  PASSING 
--    fcd.xml_data
--  COLUMNS
--    long_term_end_year NUMBER PATH 'YEAR/text()'
--) ltey ON 1=1
WHERE EXISTS (
  SELECT 1
  FROM fcs_migration.application_versions av
  WHERE av.id = xfcd.fcd_id
)
--AND xfcd.application_type IN ('FCON', 'VCON')
--AND xfcd.app_length = 'ANNUAL'
--AND xfcd.app_length = 'LONG_TERM'
--AND xfcd.app_length = 'SHORT_TERM'
ORDER BY xfcd.fcd_id ASC
)
SELECT t.*
FROM t
--WHERE (t.consent_length = 'ANNUAL' AND t.annual_consent_year IS NULL)
--OR (t.consent_length = 'LONG_TERM' AND (t.long_term_start_year IS NULL OR t.long_term_end_year IS NULL))
--OR (t.consent_length = 'SHORT_TERM' AND (t.short_term_start_date IS NULL OR t.short_term_end_date IS NULL))
