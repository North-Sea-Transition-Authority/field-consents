
-- TODO
--
-- 1) We have Long Term Flare and Vent cases in the legacy system but not in the
--    new system. Data is being migrated but we have no long term flare/vent screens
--    to show the data. Also the new screen likely won't cope well with these cases.
--    This all needs screen testing before deciding what to do.

-- 2) Some Vent apps have COVER_INFO/TERMINAL_NAME and TERMINAL_LOCATION data, these then don't have
--    the report or consent data. Need to investigate these. See action-onUpdateFieldLocation,
--    seem to change the APP_TYPE_DEFAULT if SHORT_TERM/ANNUAL and FIELD_LOCATION= IS or SNS
--
-- 3) Which legacy role maps to the new Case officer role?
--
-- 4) Need to update the flare/vent gas data prompts for Standard density when the unit is g/mol.
--    Instead of: Standard density (kg/m3)
--    Should read: Stream Mol Wt (g/mol)
--    Maybe add a gas data prompt in FlareVentUnit?
-- 
-- 5) Ensure IS_ACE_APPLICATION rules are ok with NSTA - already messaged on Teams
-- 
-- 6) For EIA data what about the FCON and VCON data in the legacy system? The new FCS apps will
--    only show/ask for this data for PRODUCTION apps
--
-- 7) application_assets - need to classify the assets PRIMARY and SECONDARY for when the LOCATION
--    assets doesn't match one of the main application assets -  already messaged NSTA on Teams for a rule
--
--
--
-- NOTES
-- 1) for a variation the fc_id stays the same (the variation no is on the detail row
--    and therefore we can have multiple details with the same version_no but different variation_no)
--    the ref number matches the fc_id in the legacy system
--    ? do we need to move the variation no from the applications table to the application versions table? NO
-- 

------------------------
-- New FCS system tables
------------------------

-- APPLICATION FORM TABLES
--DONE applications
--DONE application_versions
--DONE consent_lengths
--PARTIALLY DONE (check TODOs) application_assets
--DONE application_asset_licences
--DONE application_units
--PARTIALLY DONE (check TODOs) application_flags
--NA application_rationale (LOCATION assets migrated but we don't have the data for this table)

--PARTIALLY DONE (check TODOs) application_eia_directions
--application_supporting_information

--annual_production_months
--long_term_production_years
--short_term_production_months

--flare_annual_months
--flare_report_gas_data
--flare_report_months
--flare_report_periods
--flare_short_term_months
--flares

--vent_annual_months
--vent_report_gas_data
--vent_report_months
--vent_report_periods
--vent_short_term_months
--vents


-- CASE_PROCESSING TABLES
--application_case_notes
--application_consultations
--application_technical_reviews
--application_updates
--application_withdrawals
--NA application_work_area_priorities

-- FILE UPLOADS?
--file_upload_library_flyway
--file_upload_library_shedlock
--file_upload_library_uploaded_files
--file_upload_library_uploaded_files_aud

-- TEAMS?
--team_member_roles
--teams

-- AUDIT TABLES
--NA application_consultations_aud
--NA application_flags_aud
--NA application_technical_reviews_aud
--NA application_updates_aud
--NA application_versions_aud
--NA audit_revisions

-- OTHER
--NA flyway_schema_history



-- the ref_number can be constructed as below so no need to migrate as the parts will be migrated
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
SELECT ap.*, av.*, cl.*, au.*
FROM applications ap
JOIN application_versions av ON av.application_id = ap.id
JOIN consent_lengths cl ON cl.application_version_id = av.id
JOIN application_units au ON au.application_version_id = av.id
--WHERE av.cached_primary_operator_name IS NULL
WHERE ap.type = 'VENT'
AND cl.consent_length = 'LONG_TERM'
ORDER BY ap.application_no ASC, ap.id ASC, av.id ASC
/

SELECT aa.*, aal.*
FROM application_assets aa
ORDER BY aa.application_version_id, aa.id
/
SELECT aa.*, aal.*
FROM application_assets aa
LEFT JOIN application_asset_licences aal ON aal.application_asset_id = aa.id
WHERE (aa.asset_role IS NULL OR aa.asset_role IN ('PRIMARY', 'SECONDARY'))
--WHERE aa.asset_role = 'LOCATION'
--AND aal.id IS NOT NULL
--ORDER BY aa.application_version_id, aa.id
ORDER BY aal.id
/

SELECT af.*, ap.type
FROM applications ap
JOIN application_versions av ON av.application_id = ap.id
LEFT JOIN application_flags af ON af.application_version_id = av.id AND af.flag_type = 'WILL_GAS_BE_INJECTED' 
--WHERE ap.type = 'PRODUCTION'
--AND af.id IS NULL
/
WITH base AS (
  SELECT --av.*, 
    ap.type
  , (
    SELECT count(*)
    FROM application_assets aa
    WHERE aa.application_version_id = av.id
    AND (aa.asset_role IS NULL OR aa.asset_role IN ('PRIMARY', 'SECONDARY'))
    ) asset_count
  , af.*
  FROM applications ap
  JOIN application_versions av ON av.application_id = ap.id
  LEFT JOIN application_flags af ON af.application_version_id = av.id AND af.flag_type = 'HAS_SECONDARY_ASSETS' 
  --WHERE ap.type IN ('FLARE', 'VENT')
)
SELECT b.*
FROM base b
-- should get no rows with these conditions
WHERE (b.type IN ('FLARE', 'VENT') AND b.asset_count = 1 AND b.flag_value = 'true')
OR (b.asset_count > 1 AND b.flag_value = 'false')
OR (b.type IN ('PRODUCTION') AND b.asset_count = 1 AND b.flag_value IS NOT NULL)
/

SELECT *
FROM application_eia_directions
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
FROM fcs_migration.application_versions av
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = av.id
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
--WHERE xfcd.application_type IN ('FCON', 'VCON')
--AND xfcd.app_length = 'ANNUAL'
--AND xfcd.app_length = 'LONG_TERM'
--AND xfcd.app_length = 'SHORT_TERM'
ORDER BY xfcd.fcd_id ASC
)
SELECT t.*
FROM t;
--WHERE (t.consent_length = 'ANNUAL' AND t.annual_consent_year IS NULL)
--OR (t.consent_length = 'LONG_TERM' AND (t.long_term_start_year IS NULL OR t.long_term_end_year IS NULL))
--OR (t.consent_length = 'SHORT_TERM' AND (t.short_term_start_date IS NULL OR t.short_term_end_date IS NULL))
/


--
-- application_assets
--
--  id                         INTEGER PRIMARY KEY
--, application_version_id     INTEGER NOT NULL
--                             CONSTRAINT app_assets_fk1_version_id
--                             REFERENCES application_versions
--, field_id                   INTEGER
--, cached_field_name          VARCHAR2(4000)
--, terminal_id                INTEGER
--, cached_terminal_name       VARCHAR2(4000)
--, asset_role                 VARCHAR2(4000) NOT NULL
--, asset_no                   INTEGER
--, asset_operator_ou_id       INTEGER NOT NULL
--, cached_asset_operator_name VARCHAR2(4000)

-- find how many fields are in an application version
WITH c AS (
  SELECT count(*) field_count, xfcf.fcd_id
  FROM envmgr.xview_field_consent_fields xfcf
  GROUP BY xfcf.fcd_id
  --HAVING count(*) > 1
  --ORDER BY 2
)
-- find where the facility location matches one of the fields being consented
, fl AS (
  SELECT fcd.id fcd_id, xfcf.field_id primary_field_id
  FROM envmgr.field_consent_details fcd
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT'
    PASSING
      fcd.xml_data
    COLUMNS
      facilities_location_field_id INTEGER PATH 'COVER_INFO/FACILITIES_LOCATION/text()' -- this can be a pick of any field
  ) ff
  JOIN envmgr.xview_field_consent_fields xfcf ON xfcf.fcd_id = fcd.id AND xfcf.field_id = ff.facilities_location_field_id
)
, base AS (
  SELECT
    null id
  , xfcd.fcd_id application_version_id
  , xfcf.field_id field_id
  , f.name cached_field_name
  --, NULL terminal_id
  --, NULL cached_terminal_name
  , CASE
    WHEN c.field_count = 1 THEN 'PRIMARY'
    --WHEN xfcd.application_type = 'PCON' THEN 'PRIMARY'
    --WHEN xfcf.field_operator_ou_id != xfcd.operator_ou_id THEN 'SECONDARY'
    --WHEN xfcf.field_operator_ou_id = xfcd.operator_ou_id THEN 'PRIMARY'
    WHEN xfcf.field_id = fl.primary_field_id THEN 'PRIMARY'
    WHEN fl.primary_field_id IS NOT NULL AND xfcf.field_id != fl.primary_field_id THEN 'SECONDARY'
    ELSE NULL -- TODO will have to solve these later
    END asset_role
  --, c.field_count
  --, NULL asset_no -- will add after initial insert
  , coalesce(xfcf.field_operator_ou_id, xfcd.operator_ou_id) asset_operator_ou_id -- this is NULL for PRODUCTION, use the primary operator
  , ou.name cached_asset_operator_name
  --, xfcd.status
  --, xfcd.application_type
  --, xfcd.ref_number
  --, xfcd.operator_ou_id -- this is the field and primary operator for PRODUCTION apps
  --, ff.facilities_location_field_id -- this doesn't have to be one of the consented fields so can't be used to find the primary field
  --, fl.primary_field_id
  --, ff.facilities_location_field_id
  FROM fcs_migration.application_versions av
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = av.id
  JOIN envmgr.field_consent_details fcd ON fcd.id = xfcd.fcd_id
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT'
    PASSING
      fcd.xml_data
    COLUMNS
      facilities_location_field_id INTEGER PATH 'COVER_INFO/FACILITIES_LOCATION/text()' -- this can be a pick of any field
  ) ff
  JOIN envmgr.xview_field_consent_fields xfcf ON xfcf.fcd_id = xfcd.fcd_id
  JOIN decmgr.xview_organisation_units ou ON ou.organ_id = coalesce(xfcf.field_operator_ou_id, xfcd.operator_ou_id)
  JOIN devukmgr.fields f ON f.field_identifier = xfcf.field_id
  JOIN c ON c.fcd_id = xfcd.fcd_id
  LEFT JOIN fl ON fl.fcd_id = xfcd.fcd_id
  --WHERE (fcd.status, fcd.version_status) NOT IN (
  --  ('INPROGRESS', 'PENDING') -- an unsubmitted application update (for any version/variation) (don't migrate)
  --, ('INPROGRESS', 'CURRENT') -- an unsubmitted application (version 1 variation 0) (don't migrate)
  --)
  --AND fcd.fc_id IS NOT NULL -- implies never submitted (don't migrate)
  --AND xfcf.field_operator_ou_id IS NOT NULL
  --AND xfcd.application_type = 'PCON'
  --AND xfcf.field_operator_ou_id != xfcd.operator_ou_id
  --AND fl.primary_field_id IS NULL
  ORDER BY xfcd.fcd_id ASC
)
SELECT
  b.id
, b.application_version_id
, b.field_id
, b.cached_field_name
, b.asset_role
, CASE b.asset_role
  WHEN 'SECONDARY' THEN
    RANK () OVER (PARTITION BY b.application_version_id, b.asset_role ORDER BY b.cached_field_name)
  ELSE NULL
  END asset_no
, b.asset_operator_ou_id
, b.cached_asset_operator_name
FROM base b
--ORDER BY b.application_version_id DESC
/

--
-- try and work out the remaining asset_role's and asset_no's
--
WITH base AS (
  SELECT aa.*
  , RANK () OVER (PARTITION BY aa.application_version_id ORDER BY aa.cached_field_name) field_order
  FROM application_assets aa
  WHERE aa.asset_role IS NULL
  ORDER BY aa.application_version_id, aa.id
)
SELECT
  b.id
, b.application_version_id
, b.field_id
, b.cached_field_name
, CASE b.field_order
  WHEN 1 THEN 'PRIMARY'
  ELSE 'SECONDARY'
  END asset_role
, CASE b.field_order
  WHEN 1 THEN NULL
  ELSE b.field_order - 1
  END asset_no
, b.asset_operator_ou_id
, b.cached_asset_operator_name
, b.field_order
FROM base b
/

--
-- LOCATION assets
--
SELECT
  null id
, fcd.id application_version_id
, ff.facilities_location_field_id field_id
, f.name cached_field_name
, 'LOCATION' asset_role
, fov.operator_id asset_operator_ou_id
, fov.operator_name cached_asset_operator_name
--, fcd.status
--, fcd.application_type
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    facilities_location_field_id INTEGER PATH 'COVER_INFO/FACILITIES_LOCATION/text()'
) ff
JOIN devukmgr.fields f ON f.field_identifier = ff.facilities_location_field_id
LEFT JOIN devukmgr.field_operator_view fov ON f.field_identifier = fov.field_id
WHERE ff.facilities_location_field_id IS NOT NULL
ORDER BY fcd.id ASC
/

--
-- application_asset_licences
--
--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT app_asset_licences_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, application_asset_id   INTEGER NOT NULL
--                         CONSTRAINT app_asset_licences_fk2_aa_id
--                         REFERENCES fcs_migration.application_assets
--, licence_id             INTEGER NOT NULL
--, cached_licence_ref     VARCHAR2(4000) NOT NULL

WITH licences AS (
  SELECT
    plm.id licence_id
  , (pcdp.licence_type || pcdp.licence_no) licence_number
  FROM pedmgr.ped_licence_master plm
  JOIN pedmgr.ped_current_data_points pcdp ON pcdp.licence_no = plm.licence_no AND pcdp.licence_type = plm.licence_type
  WHERE plm.system_status = 'LIVE'
  AND  pcdp.ped_sim_id = 0
)
SELECT
  null id
, aa.application_version_id
, aa.id application_asset_id
, li.licence_id
, li.licence_number cached_licence_ref
--, xfcd.ref_number
--, xfcd.status
--, xfcd.application_type
--, xfcl.*
--, li.*
--, xfcd.*
FROM fcs_migration.application_assets aa
--JOIN envmgr.xview_field_consent_fields xfcf ON xfcf.fcd_id = aa.application_version_id AND xfcf.field_id = aa.field_id
JOIN envmgr.xview_field_consent_licences xfcl ON xfcl.fcd_id = aa.application_version_id AND xfcl.field_id = aa.field_id
--JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = aa.application_version_id
JOIN licences li ON li.licence_number = xfcl.licence_number
WHERE (aa.asset_role IS NULL OR aa.asset_role IN ('PRIMARY', 'SECONDARY')) -- TODO remove NULL check, the NULL here is to account for the unclassified application_assets
--AND xfcl.field_id IS NULL
--AND li.licence_id IS NULL;
/

SELECT xfcd.*
FROM envmgr.xview_field_consent_details xfcd
WHERE xfcd.fc_id = 1337;
/
SELECT av.*
FROM application_versions av
WHERE av.id IN (2590, 3469, 3470, 3550);
/


--
-- application_units
--
--  id INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT application_units_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, flare_category_unit    VARCHAR2(4000)
--, vent_category_unit     VARCHAR2(4000)
--, production_oil_unit    VARCHAR2(4000)
--, production_gas_unit    VARCHAR2(4000)
--, flare_gas_density_unit VARCHAR2(4000)
--, flare_gas_content_unit VARCHAR2(4000)
--, vent_gas_density_unit  VARCHAR2(4000)
--, vent_gas_content_unit  VARCHAR2(4000)
--);

-- Unit rules

-- Production
-- Short term/Annual
-- Oil (min/max) - scm/month
-- Gas (min/max) - kscm/month

-- Production
-- Long term
-- Oil (min/max) - kscm/day
-- Gas (min/max) - kscm/day

-- Flare and Vent
-- Short term/Annual (report or consent data, either 123(flare)/Unignited Vent(s) or ABC (flare or vent))
-- tonnes/month

-- Flare and Vent
-- Long term (consent data only no report, gas only no categories)
-- tonnes/day

-- Flare and Vent Report Gas Data
-- Stream Mol Wt (g/mol)
-- Inert Gas Content (mol %)    
-- Hydrocarbon content (mol %)

SELECT
  null id
, av.id application_version_id
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'KSCM_PER_DAY'
  WHEN 'ANNUAL' THEN 'SCM_PER_MONTH'
  WHEN 'SHORT_TERM' THEN 'SCM_PER_MONTH'
  END production_oil_unit
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'KSCM_PER_DAY'
  WHEN 'ANNUAL' THEN 'KSCM_PER_MONTH'
  WHEN 'SHORT_TERM' THEN 'KSCM_PER_MONTH'
  END production_gas_unit
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
WHERE a.type = 'PRODUCTION'
/
SELECT
  null id
, av.id application_version_id
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'TONNES_PER_DAY'
  WHEN 'ANNUAL' THEN 'TONNES_PER_MONTH'
  WHEN 'SHORT_TERM' THEN 'TONNES_PER_MONTH'
  END flare_category_unit
, 'G_PER_MOL' flare_gas_density_unit
, 'MOL_PERCENTAGE' flare_gas_content_unit
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
WHERE a.type = 'FLARE'
/
SELECT
  null id
, av.id application_version_id
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'TONNES_PER_DAY'
  WHEN 'ANNUAL' THEN 'TONNES_PER_MONTH'
  WHEN 'SHORT_TERM' THEN 'TONNES_PER_MONTH'
  END vent_category_unit
, 'G_PER_MOL' vent_gas_density_unit
, 'MOL_PERCENTAGE' vent_gas_content_unit
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
WHERE a.type = 'VENT';
/

--
-- application_flags
--
--
--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT app_flags_fk1_version_id
--                         REFERENCES fcs_migration.application_versions
--, flag_type              VARCHAR2(4000) NOT NULL
--, flag_value             VARCHAR2(5) NOT NULL -- true/false

-- WILL_GAS_BE_INJECTED
SELECT --count(*) -- fcd.fc_id, fcd.id, gi.*, fcd.application_type
  null id
, fcd.id application_version_id
, 'WILL_GAS_BE_INJECTED'
, gas.injection_question
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    injection_question VARCHAR(5) PATH 'COVER_INFO/INJECTION_QUESTION/text()'
) gas
WHERE fcd.application_type = 'PCON' 
AND gas.injection_question IS NOT NULL;
/

-- HAS_SECONDARY_ASSETS("Do you have any additional fields to add?"),
SELECT
  aa.application_version_id
, 'HAS_SECONDARY_ASSETS' flag_type
, CASE
  WHEN count(*) > 1 THEN 'true'
  ELSE 'false'
  END flag_value
--, count(*)
--, st.join(stagg(aa.cached_field_name)) fields
--, st.join(stagg(aa.asset_role)) asset_roles
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.application_assets aa ON aa.application_version_id = av.id
WHERE a.type IN ('FLARE', 'VENT')
AND (aa.asset_role IS NULL OR aa.asset_role IN ('PRIMARY', 'SECONDARY')) -- NULL means an unclassified PRIMARY or SECONDARY asset at the moment
GROUP BY aa.application_version_id;
--HAVING st.join(stagg(aa.asset_role)) IS NULL -- != 'PRIMARY'
/


-- IS_ACE_APPLICATION("Is this an ACE application?")
SELECT
  av.id application_version_id
, 'IS_ACE_APPLICATION' flag_type
, CASE cl.consent_length
  WHEN 'SHORT_TERM' THEN 'false'
  WHEN 'ANNUAL' THEN
    CASE
    WHEN cl.annual_consent_year = to_number(to_char(av.submitted_date_time, 'YYYY')) + 1 THEN 'true'
    ELSE 'false'
    END
  WHEN 'LONG_TERM' THEN
    CASE
    WHEN cl.long_term_start_year = to_number(to_char(av.submitted_date_time, 'YYYY')) + 1 THEN 'true'
    ELSE 'false'
    END
  END flag_value
, av.submitted_date_time
, to_number(to_char(av.submitted_date_time, 'YYYY')) submitted_year
, cl.annual_consent_year
, cl.long_term_start_year
, cl.consent_length
, a.application_no
, a.variation_no
, av.*
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
WHERE av.submitted_date_time IS NOT NULL;
--AND cl.consent_length IN ('ANNUAL', 'LONG_TERM')
/

--
-- application_eia_directions
--

-- TODO there is data for Flare and Vent apps but this section/question doesn't appear on the new system unless it's a production app
SELECT fcd.id, fcd.fc_id, fcd.application_type
, eia.*
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    project_under_eia_regs VARCHAR(5) PATH 'ADDITIONAL_INFO/PROJECT_UNDER_EIA_REGS/text()'
) eia
WHERE eia.project_under_eia_regs IS NOT NULL;
/

--
-- application_supporting_information
--

SELECT fcd.id, fcd.fc_id, fcd.application_type, fcd.version_no
, hi.*
--, length(hi.content_text) content_length
--, dbms_lob.substr(hi.content_text, 4000) content_text_substr
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
--CROSS JOIN XMLTABLE(
--  '/FIELD_CONSENT/ADDITIONAL_INFO/HISTORY_LIST/HISTORY_ITEM[./EDITABLE_FLAG/text() = ''true'']'
--  PASSING
--    fcd.xml_data
--  COLUMNS
--    fcd_id INTEGER PATH 'FCD_ID/text()'
--  , last_updated_by VARCHAR(4000) PATH './LAST_UPDATED_BY/text()'
--  , last_updated_date VARCHAR(4000) PATH './LAST_UPDATED_DATE/text()'
--  , type VARCHAR(4000) PATH './TYPE/text()'
--  , content_text CLOB PATH './CONTENT/text()'
--  , editable_flag VARCHAR(4000) PATH './EDITABLE_FLAG/text()'
--) hi
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_INFO/HISTORY_LIST'
  PASSING
    fcd.xml_data
  COLUMNS
    note_text CLOB PATH './HISTORY_ITEM[TYPE/text()="ADDITIONAL_NOTE"][EDITABLE_FLAG/text()="true"]/CONTENT/text()'
--  , note_editable_flag VARCHAR(4000) PATH './HISTORY_ITEM[TYPE/text()="ADDITIONAL_NOTE"][last()]/EDITABLE_FLAG/text()'
  , imp_note_text CLOB PATH './HISTORY_ITEM[TYPE/text()="IMPROVEMENT_NOTE"][EDITABLE_FLAG/text()="true"]/CONTENT/text()'
--  , imp_note_editable_flag VARCHAR(4000) PATH './HISTORY_ITEM[TYPE/text()="IMPROVEMENT_NOTE"][last()]/EDITABLE_FLAG/text()'
) hi
--WHERE EXISTS (
--  SELECT 1
--  FROM fcs_migration.application_versions av
--  WHERE av.id = fcd.id
--)
--AND hi.fcd_id IS NOT NULL
--AND (hi.editable_flag = 'false' AND hi.fcd_id = fcd.id)
--AND hi.fcd_id = fcd.id
--AND length(hi.content_text) > 4000
--AND (hi.note_editable_flag != 'true' OR hi.imp_note_editable_flag != 'true')
ORDER BY 2 DESC, 1 DESC