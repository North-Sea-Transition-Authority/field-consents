
-- TODO
--
-- 1) We have Long Term Flare and Vent cases in the legacy system but not in the
--    new system. Data is being migrated but we have no long term flare/vent screens
--    to show the data. Also the new screen likely won't cope well with these cases.
--    This all needs screen testing before deciding what to do.

-- 2) Some Vent apps have COVER_INFO/TERMINAL_NAME and TERMINAL_LOCATION data, these then don't have
--    the report or consent data. Need to investigate these. See action-onUpdateFieldLocation,
--    seem to change the APP_TYPE_DEFAULT if SHORT_TERM/ANNUAL and FIELD_LOCATION= IS or SNS
--    These types of apps are no longer allowed, i.e. we now want the report and consent data
--    values.
--    A good example of the terminal version of these Vent apps is: CLIPPER SOUTH (INEOS UK SNS LIMITED)
--
-- 3) Need to update the flare/vent gas data prompts for Standard density when the unit is g/mol.
--    Instead of: Standard density (kg/m3)
--    Should read: Stream Mol Wt (g/mol)
--    Maybe add a gas data prompt in FlareVentUnit?
-- 
-- 4) For EIA data what about the FCON and VCON data in the legacy system? The new FCS apps will
--    only show/ask for this data for PRODUCTION apps. See 5)
--
-- 5) For app data which we no longer has a place, we need to migrate to another specific migration data table
--    and display in the app summary in some way
--    Examples:
--    - production annual and long term uplift %
--      /*/ANNUAL_PRODUCTION/UPLIFT
--      /*/LONG_TERM_PRODUCTION/UPLIFT
--    - flare category 1/2/3 values (report and consents pages)
--      /*/CONSENT/CONSENT_DATA_LIST/CONSENT_DATA/CATEGORY_1 | CATEGORY_2 | CATEGORY_3 (Annual)
--      /*/SHORT_TERM_CONSENT/CONSENT_DATA_LIST/CONSENT_DATA/CATEGORY_1 | CATEGORY_2 | CATEGORY_3 (Short term)
--    - flare - long term data
--      /*/LONG_TERM_CONSENT/DATA_LIST/DATA/YEAR | GAS
--
-- 6) LOCATION application_assets - some have no operator (see devukmgr.field_operator_view), so we can't migrate yet
--    Issues: on dev: SUTTON MANOR COAL MINE VENT
--            on st: SUTTON MANOR COAL MINE VENT
--                   CLIPPER SOUTH
--                   BENTLEY
--                   ALVHEIM
--                   ALMA
--                   BOULTON H
--            on uat and live: ALVHEIM
--                             STATFJORD(CROSS BORDER)
--
-- NOTES
-- a) for a variation the fc_id stays the same (the variation no is on the detail row
--    and therefore we can have multiple details with the same version_no but different variation_no)
--    the ref number matches the fc_id in the legacy system
--    Do we need to move the variation_no from the applications table to the application_versions table?
--    => No. We will have a new master id for when we create a new variation.
--    Note - the application from data should be copied to the new variation, but not the rest of the case processing
--    data, i.e. case notes, tech reviews, consultations etc (this is in line with the legacy system) 
--
-- b) The IS_ACE_APPLICATION rules below are confirmed ok with NSTA
--
-- c) application_assets - to classify the assets PRIMARY and SECONDARY for when the LOCATION
--    assets doesn't match one of the main application assets, we should just use the first Field as listed
--    in the application from for the PRIMARY. All the other fields should be SECONDARY.
--    Good example: BIRCH 
--
-- d) The assigned Case officer should come from the Range 6 or Administrator for legacy apps.
--    TODO - fix queries for this
--    We need to ensure that the regulator teams are setup with appropriate case officers. Specifically
--    the currently assigned case officers for "Submitted" legacy cases must be setup so the app processing
--    can continue in the new system.
--
-- e) For legacy Short Term Flare/Vent 1_2_3 cases there are no reports (the dom elements exist
--    but there are no figures as you can't get to the report page for 1_2_3 cases).
--
-- f) Ensure NSTA are aware that the totals and averages are not being migrated, but instead
--    we will calculate on the fly when showing the applications summary. Note - specifically
--    highlight that the shutdown days are not now taken off the total days when working out
--    the daily averages.

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
--DONE application_flags
--NA application_rationale (LOCATION assets migrated but we don't have the data for this table)

--PARTIALLY DONE (check TODOs) application_eia_directions
--DONE application_supporting_information

--DONE long_term_production_years
--DONE annual_production_months
--DONE short_term_production_months

--DONE flare_annual_months
--DONE flare_short_term_months
--DONE flare_report_gas_data
--DONE flare_report_periods
--DONE flare_report_months
--flares

--vent_annual_months
--vent_short_term_months
--vent_report_gas_data
--vent_report_periods
--vent_report_months
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
--, asset_role                 VARCHAR2(4000) NOT NULL
--, asset_no                   INTEGER
--, asset_operator_ou_id       INTEGER NOT NULL
--, cached_asset_operator_name VARCHAR2(4000)
--, asset_type                 VARCHAR2(4000)
--, asset_id                   INTEGER
--, cached_asset_name          VARCHAR2(4000)


-- find how many fields are in an application version
WITH c AS (
  SELECT count(*) field_count, xfcf.fcd_id
  FROM envmgr.xview_field_consent_fields xfcf
  GROUP BY xfcf.fcd_id
--  HAVING count(*) > 1
--  ORDER BY 2
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
  , CASE
    WHEN c.field_count = 1 THEN 'PRIMARY'
    WHEN xfcf.field_id = fl.primary_field_id THEN 'PRIMARY'
    -- where the location field isn't a field in the list just use the first field for the PRIMARY
    WHEN fl.primary_field_id IS NULL AND xfcf.field_rownum = 1 THEN 'PRIMARY'
    ELSE 'SECONDARY'
    END asset_role
  , coalesce(xfcf.field_operator_ou_id, xfcd.operator_ou_id) asset_operator_ou_id -- this is NULL for PRODUCTION, use the primary operator
  , ou.name cached_asset_operator_name
  , xfcf.field_id asset_id
  , f.name cached_asset_name
  , xfcf.field_rownum
--  , CASE WHEN xfcf.field_id != fl.primary_field_id AND xfcf.field_rownum = 1 THEN 'PRIMARY IS NOT LOCATION FIELD' END location_flag
--  , CASE WHEN xfcf.field_id = fl.primary_field_id AND xfcf.field_rownum != 1 THEN 'PRIMARY NOT FIRST' END primary_not_first
  , fl.primary_field_id
  , ff.facilities_location_field_id
  --, c.field_count
  --, xfcd.status
  --, xfcd.application_type
  --, xfcd.ref_number
  , xfcd.operator_ou_id primary_operator_ou_id -- this is the field and primary operator for PRODUCTION apps
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
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT/COVER_INFO/FIELD_LIST/FIELD'
    PASSING
      fcd.xml_data
    COLUMNS
      field_rownum FOR ORDINALITY
    , field_id INTEGER PATH './FIELD_INFO/FIELD_ID/text()'
    , field_operator_ou_id INTEGER PATH 'FIELD_OPERATOR_OU_ID/text()' 
  ) xfcf
  --JOIN envmgr.xview_field_consent_fields xfcf ON xfcf.fcd_id = xfcd.fcd_id
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
, b.asset_role
, CASE b.asset_role
  WHEN 'SECONDARY' THEN
    RANK () OVER (PARTITION BY b.application_version_id, b.asset_role ORDER BY b.field_rownum)
  ELSE NULL
  END asset_no
, b.asset_operator_ou_id
, b.cached_asset_operator_name
, 'FIELD' asset_type
, b.asset_id
, b.cached_asset_name
, b.field_rownum
, CASE b.asset_role
  WHEN 'SECONDARY' THEN
    RANK () OVER (PARTITION BY b.application_version_id, b.asset_role ORDER BY b.cached_asset_name)
  ELSE NULL
  END asset_no_if_alpha
--, b.location_flag
--, b.primary_not_first
, b.primary_field_id
, b.facilities_location_field_id
, b.primary_operator_ou_id
, CASE WHEN b.asset_role = 'PRIMARY' AND b.field_rownum != 1 THEN 'PRIMARY NOT FIRST' END primary_not_first
, CASE WHEN b.asset_role = 'PRIMARY' AND b.primary_field_id IS NULL THEN 'PRIMARY NOT LOCATION FIELD' END primary_not_location_field
FROM base b
--WHERE b.asset_role = 'PRIMARY' AND b.asset_operator_ou_id != b.primary_operator_ou_id
--WHERE b.asset_role = 'PRIMARY' AND b.field_rownum != 1
--ORDER BY b.application_version_id DESC
/


--
-- LOCATION assets
--
SELECT
  null id
, fcd.id application_version_id
, ff.facilities_location_field_id asset_id
, f.name cached_asset_name
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
--AND fov.operator_id IS NULL
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
WHERE aa.asset_role IN ('PRIMARY', 'SECONDARY')
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

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT application_supporting_information_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, notes                  CLOB NOT NULL
--, erap_notes             CLOB


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
--WHERE (hi.note_editable_flag != 'true' OR hi.imp_note_editable_flag != 'true')
--WHERE (hi.note_text IS NOT NULL OR hi.imp_note_text IS NOT NULL)
ORDER BY 2 DESC, 1 DESC;
/

--
-- long_term_production_years
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT long_term_prod_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, year                   INTEGER NOT NULL
--, oil_min_value          NUMBER NOT NULL
--, oil_max_value          NUMBER NOT NULL
--, gas_min_value          NUMBER NOT NULL
--, gas_max_value          NUMBER NOT NULL

SELECT
  fcd.id application_version_id
, ltp.year
, ltp.oil_min_value
, ltp.oil_max_value
, ltp.gas_min_value
, ltp.gas_max_value
, fcd.fc_id
FROM fcs_migration.application_versions av
--JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/LONG_TERM_PRODUCTION/PRODUCTION_DATA_LIST/PRODUCTION_DATA'
  PASSING
    fcd.xml_data
  COLUMNS
    year INTEGER PATH './YEAR/text()'
  , oil_min_value NUMBER PATH './OIL_MIN/text()'
  , oil_max_value NUMBER PATH './OIL/text()'
  , gas_min_value NUMBER PATH './GAS_MIN/text()'
  , gas_max_value NUMBER PATH './GAS/text()'
) ltp
WHERE fcd.application_type = 'PCON'
AND xfcd.app_length = 'LONG_TERM'
AND coalesce(ltp.oil_min_value, ltp.oil_max_value, ltp.gas_max_value, ltp.gas_max_value) IS NOT NULL
--AND ltp.year IS NULL --(ltp.gas_max_value IS NULL OR ltp.oil_max_value IS NULL)
ORDER BY fcd.id, ltp.year
/

--SELECT xfcd.*
--FROM envmgr.xview_field_consent_details xfcd
--WHERE xfcd.fc_id IN (282, 214, 922)
--/
--SELECT av.*
--FROM fcs_migration.application_versions av
--WHERE av.application_id = 282
--/
--SELECT *
--FROM fcs_migration.long_term_production_years py
--JOIN fcs_migration.application_versions av ON av.id = py.application_version_id
--JOIN fcs_migration.applications a ON a.id = av.application_id
--WHERE (py.oil_max_value IS NULL OR py.gas_max_value IS NULL)
--/

--
-- annual_production_months
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT annual_prod_fk1_version_id
--                         REFERENCES fcs_migration.application_versions
--, year                   INTEGER NOT NULL
--, month                  VARCHAR2(4000) NOT NULL
--, oil_min_value          NUMBER NOT NULL
--, oil_max_value          NUMBER NOT NULL
--, gas_min_value          NUMBER NOT NULL
--, gas_max_value          NUMBER NOT NULL

SELECT
  fcd.id application_version_id
, xfcd.application_year
, upper(trim(ap.description)) month
, ap.oil_min_value
, ap.oil_max_value
, ap.gas_min_value
, ap.gas_max_value
, fcd.fc_id
, fcd.status
, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ANNUAL_PRODUCTION/PRODUCTION_DATA_LIST/PRODUCTION_DATA[./TYPE/text()="MONTH"]'
  PASSING
    fcd.xml_data
  COLUMNS
    type VARCHAR2(4000) PATH 'TYPE/text()' -- MONTH, TOTAL, AVERAGE1, AVERAGE2
  , pd_rownum FOR ORDINALITY
  , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
  , days INTEGER PATH './DAYS/text()'
  , oil_min_value NUMBER PATH './OIL_MIN/text()'
  , oil_max_value NUMBER PATH './OIL/text()'
  , gas_min_value NUMBER PATH './GAS_MIN/text()'
  , gas_max_value NUMBER PATH './GAS/text()'
) ap
WHERE fcd.application_type = 'PCON'
AND xfcd.app_length = 'ANNUAL'
AND coalesce(ap.oil_min_value, ap.oil_max_value, ap.gas_max_value, ap.gas_max_value) IS NOT NULL
--AND (ap.pd_rownum, upper(trim(ap.description))) IN (
--  (1, 'JANUARY')  
--, (2, 'FEBRUARY') 
--, (3, 'MARCH')    
--, (4, 'APRIL')    
--, (5, 'MAY')      
--, (6, 'JUNE')
--, (7, 'JULY')
--, (8, 'AUGUST')
--, (9, 'SEPTEMBER')
--, (10, 'OCTOBER')
--, (11, 'NOVEMBER')
--, (12, 'DECEMBER')
--)
ORDER BY fcd.id, ap.pd_rownum;
/

--
-- short_term_production_months
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , stp.pd_rownum
  , stp.data_year
  , upper(trim(stp.description)) month
  , cl.short_term_start_date
  , cl.short_term_end_date
  , stp.oil_min_value
  , stp.oil_max_value
  , stp.gas_min_value
  , stp.gas_max_value
--  , fcd.fc_id
--  , fcd.status
--  , fcd.version_status
  FROM fcs_migration.application_versions av
--  JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT'
    PASSING
      fcd.xml_data
    COLUMNS
    short_term_start_date DATE PATH 'COVER_INFO/STC_START_DATE/text()'
  , short_term_end_date DATE PATH 'COVER_INFO/STC_END_DATE/text()'
  ) cl
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT/SHORT_TERM_CONSENT/CONSENT_DATA_LIST/CONSENT_DATA[./TYPE/text()="MONTH"]'
    PASSING
      fcd.xml_data
    COLUMNS
      type VARCHAR2(4000) PATH 'TYPE/text()' -- MONTH, TOTAL, AVERAGE1, AVERAGE2
    , pd_rownum FOR ORDINALITY
    , data_year INTEGER PATH './DATA_YEAR/text()'
    , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
    , days INTEGER PATH './DAYS/text()'
    , consent_days INTEGER PATH './CONSENT_DAYS/text()'
    , oil_min_value NUMBER PATH './OIL_MIN/text()'
    , oil_max_value NUMBER PATH './OIL/text()'
    , gas_min_value NUMBER PATH './GAS_MIN/text()'
    , gas_max_value NUMBER PATH './GAS/text()'
  ) stp
  WHERE fcd.application_type = 'PCON'
  AND xfcd.app_length = 'SHORT_TERM'
  AND coalesce(stp.oil_min_value, stp.oil_max_value, stp.gas_max_value, stp.gas_max_value) IS NOT NULL
  ORDER BY fcd.id, stp.pd_rownum
)
, base2 AS (
  SELECT b.*
  , to_date('01'||b.month||b.data_year, 'DDMONTHYYYY') month_start_date
  , last_day(to_date('01'||b.month||b.data_year, 'DDMONTHYYYY')) month_end_date
  FROM base b
)
SELECT
  b.*
, greatest(b.short_term_start_date, b.month_start_date) row_start_date
, least(b.short_term_end_date, b.month_end_date) row_end_date
FROM base2 b
/

--
-- flare_annual_months
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT flare_annual_months_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, year                   INTEGER NOT NULL
--, month                  VARCHAR2(4000) NOT NULL
--, category_a             NUMBER NOT NULL
--, category_b             NUMBER NOT NULL
--, category_c             NUMBER NOT NULL
--, comments               VARCHAR2(4000)

SELECT
  fcd.id application_version_id
, af.cd_rownum
, xfcd.application_year
, upper(trim(af.description)) month
--, af.category_1
--, af.category_2
--, af.category_3
, af.category_a
, af.category_b
, af.category_c
--, af.total_flare_gas
, af.comments
, fcd.fc_id
, fcd.status
, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    categories VARCHAR2(4000) PATH './FLAGS/CATEGORIES/text()'
) cat
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/CONSENT/CONSENT_DATA_LIST/CONSENT_DATA[./TYPE/text()="MONTH"]'
  PASSING
    fcd.xml_data
  COLUMNS
    type VARCHAR2(4000) PATH 'TYPE/text()' -- MONTH, TOTAL, AVERAGE
  , cd_rownum FOR ORDINALITY
  , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
  , days INTEGER PATH './DAYS/text()'
--  , category_1 NUMBER PATH './CATEGORY_1/text()'
--  , category_2 NUMBER PATH './CATEGORY_2/text()'
--  , category_3 NUMBER PATH './CATEGORY_3/text()'
  , category_a NUMBER PATH './CATEGORY_A/text()'
  , category_b NUMBER PATH './CATEGORY_B/text()'
  , category_c NUMBER PATH './CATEGORY_C/text()'
--  , total_flare_gas NUMBER PATH './TOTAL_FLARE_GAS/text()'
  , comments VARCHAR2(4000) PATH './COMMENTS/text()'
) af
WHERE fcd.application_type = 'FCON'
AND xfcd.app_length = 'ANNUAL'
AND cat.categories = 'A_B_C'
--AND (upper(trim(af.description))) NOT IN (
--  'JANUARY', 'FEBRUARY', 'MARCH'    
--, 'APRIL', 'MAY', 'JUNE'
--, 'JULY', 'AUGUST', 'SEPTEMBER'
--, 'OCTOBER', 'NOVEMBER', 'DECEMBER'
--)
--AND coalesce(af.category_1, 0) + coalesce(af.category_2, 0) + coalesce(af.category_3, 0) + coalesce(af.category_a, 0) + coalesce(af.category_b, 0) + coalesce(af.category_c, 0) = af.total_flare_gas
ORDER BY fcd.id, af.cd_rownum;
/

--
-- flare_short_term_months
--

WITH base AS (
  SELECT
    fcd.id application_version_id
  , stf.cd_rownum
  , stf.data_year
  , upper(trim(stf.description)) month
  , cl.short_term_start_date
  , cl.short_term_end_date
--  , stf.category_1
--  , stf.category_2
--  , stf.category_3
  , stf.category_a
  , stf.category_b
  , stf.category_c
--  , stf.total_flare_gas
  , stf.comments
--  , fcd.fc_id
--  , fcd.status
--  , fcd.version_status
  FROM fcs_migration.application_versions av
--  JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT'
    PASSING
      fcd.xml_data
    COLUMNS
      short_term_start_date DATE PATH 'COVER_INFO/STC_START_DATE/text()'
    , short_term_end_date DATE PATH 'COVER_INFO/STC_END_DATE/text()'
    , categories VARCHAR2(4000) PATH './FLAGS/CATEGORIES/text()'
  ) cl
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT/SHORT_TERM_CONSENT/CONSENT_DATA_LIST/CONSENT_DATA[./TYPE/text()="MONTH"]'
    PASSING
      fcd.xml_data
    COLUMNS
      type VARCHAR2(4000) PATH 'TYPE/text()' -- MONTH, TOTAL, AVERAGE
    , cd_rownum FOR ORDINALITY
    , data_year INTEGER PATH './DATA_YEAR/text()'
    , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
    , days INTEGER PATH './DAYS/text()'
    , consent_days INTEGER PATH './CONSENT_DAYS/text()'
--    , category_1 NUMBER PATH './CATEGORY_1/text()'
--    , category_2 NUMBER PATH './CATEGORY_2/text()'
--    , category_3 NUMBER PATH './CATEGORY_3/text()'
    , category_a NUMBER PATH './CATEGORY_A/text()'
    , category_b NUMBER PATH './CATEGORY_B/text()'
    , category_c NUMBER PATH './CATEGORY_C/text()'
--    , total_flare_gas NUMBER PATH './TOTAL_FLARE_GAS/text()'
    , comments VARCHAR2(4000) PATH './COMMENTS/text()'
  ) stf
  WHERE fcd.application_type = 'FCON'
  AND xfcd.app_length = 'SHORT_TERM'
  AND cl.categories = 'A_B_C'
  --AND coalesce(stf.category_1, 0) + coalesce(stf.category_2, 0) + coalesce(stf.category_3, 0) + coalesce(stf.category_a, 0) + coalesce(stf.category_b, 0) + coalesce(stf.category_c, 0) = stf.total_flare_gas
  ORDER BY fcd.id, stf.cd_rownum
)
, base2 AS (
  SELECT b.*
  , to_date('01'||b.month||b.data_year, 'DDMONTHYYYY') month_start_date
  , last_day(to_date('01'||b.month||b.data_year, 'DDMONTHYYYY')) month_end_date
  FROM base b
)
SELECT
  b.*
, greatest(b.short_term_start_date, b.month_start_date) row_start_date
, least(b.short_term_end_date, b.month_end_date) row_end_date
FROM base2 b;
/

--
-- flare_report_gas_data
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  --, xfcd.application_year
  , CASE
    WHEN rd.upper_desc LIKE '%STREAM%' THEN 'DENSITY'
    WHEN rd.upper_desc LIKE '%INERT%' THEN 'INERT'
    WHEN rd.upper_desc LIKE '%HYDROCARBON%' THEN 'HYDRO'
    END data_type
  , rd.category_a
  , rd.category_b
  , rd.category_c
  --, rd.categories
  --, rd.rd_type
  --, rd.rd_rownum
  --, rd.rd_month
  --, rd.description
  --, rd.upper_desc
  --, rd.days
  --, rd.category_1
  --, rd.category_2
  --, rd.category_3
  --, rd.category_a
  --, rd.category_b
  --, rd.category_c
  --, rd.total_flare_gas
  --, rd.days_total_shutdown
  --, rd.comments
  --, fcd.fc_id
  --, fcd.application_type
  --, fcd.status
  --, fcd.version_status
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'INFO' 
  AND fcd.application_type = 'FCON'
  AND rd.categories = 'A_B_C'
  AND coalesce(rd.category_a, rd.category_b, rd.category_c) IS NOT NULL
  ORDER BY rd.fcd_id, rd.rd_rownum
)
--, unpivoted AS (
--  SELECT application_version_id, data_type||category data_type_combined, amount
--  FROM base
--  UNPIVOT(
--      amount  -- unpivot_clause
--      FOR category --  unpivot_for_clause
--      IN ( -- unpivot_in_clause
--          category_a AS '_A', 
--          category_b AS '_B', 
--          category_c AS '_C'
--      )
--  )
--)
--SELECT *
--FROM (
--  SELECT application_version_id, data_type_combined, amount
--  FROM unpivoted
--)
--PIVOT
--(
--  SUM(amount)
--  FOR data_type_combined
--  IN (
--    'DENSITY_A' AS category_a_density, 'DENSITY_B' AS category_b_density, 'DENSITY_C' AS category_c_density
--  , 'INERT_A' AS category_a_inert_percentage, 'INERT_B' AS category_b_inert_percentage, 'INERT_C' AS category_c_inert_percentage
--  , 'HYDRO_A' AS category_a_hydro_percentage, 'HYDRO_B' AS category_b_hydro_percentage, 'HYDRO_C' AS category_c_hydro_percentage
--  )
--)
SELECT
  bd.application_version_id

, bd.category_a category_a_density
, bi.category_a category_a_inert_percentage
, bh.category_a category_a_hydro_percentage

, bd.category_b category_b_density
, bi.category_b category_b_inert_percentage
, bh.category_a category_b_hydro_percentage

, bd.category_c category_c_density
, bi.category_c category_c_inert_percentage
, bh.category_a category_c_hydro_percentage
FROM base bd
JOIN base bi ON bi.application_version_id = bd.application_version_id AND bi.data_type = 'INERT'
JOIN base bh ON bh.application_version_id = bd.application_version_id AND bh.data_type = 'HYDRO'
WHERE bd.data_type = 'DENSITY'
/

--
-- flare_report_periods
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT flare_report_periods_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, report_end_month       VARCHAR2(4000) NOT NULL
--, report_end_year        INTEGER NOT NULL

WITH base AS (
  SELECT
    fcd.id application_version_id
  , xfcd.application_year
  , xfcd.app_length
  , last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY')) report_row_end_date 
  , rd.*
  , fcd.fc_id
  , fcd.application_type
  , fcd.status
  , fcd.version_status
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'FCON'
  AND rd.categories = 'A_B_C'
  AND coalesce(rd.category_a, rd.category_b, rd.category_c, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.fc_id, rd.fcd_id, rd.rd_rownum
)
, report_end AS (
  SELECT b.application_version_id, b.fc_id, max(b.report_row_end_date) report_row_end_date
  FROM base b
  GROUP BY b.application_version_id, b.fc_id
)
SELECT
  re.application_version_id, re.fc_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re
ORDER BY re.fc_id, re.application_version_id;
/

--
-- flare_report_months
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT flare_report_months_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, year                   INTEGER NOT NULL
--, month                  VARCHAR2(4000) NOT NULL
--, category_a             NUMERIC NOT NULL
--, category_b             NUMERIC NOT NULL
--, category_c             NUMERIC NOT NULL
--, shut_down_days         INTEGER NOT NULL
--, comments               VARCHAR2(4000)

WITH base AS (
  SELECT
    fcd.id application_version_id
  , xfcd.application_year
  , xfcd.app_length
  , last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY')) report_row_end_date
  , rd.*
  , fcd.fc_id
  , fcd.application_type
  , fcd.status
  , fcd.version_status
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'FCON'
  AND rd.categories = 'A_B_C'
  AND coalesce(rd.category_a, rd.category_b, rd.category_c, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
SELECT
  b.application_version_id
, to_number(to_char(b.report_row_end_date, 'YYYY')) year
, to_char(b.report_row_end_date, 'MONTH') month
, b.category_a
, b.category_b
, b.category_c
, b.days_total_shutdown
, b.comments
FROM base b;
/

