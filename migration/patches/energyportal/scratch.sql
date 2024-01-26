
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
-- 7) Do we need to migrate legacy payments?
--    CT thinks no - they can access via the portal payments screens.
--                   Also the legacy system doesn't let then see the payments from within Field Consent cases.
-- 
-- 8) Still need to consider the consent processing data. i.e. the consented figures and also the field equity partners and the
--    items that drive the Consent docs and Cover letter. I don't have anywhere for this data to go yet.
--    Plus the actual consent output documents.
--
-- 9) Need to migrate the supporting docs file uploads.
--
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
--

-- BPMMGR Intention sets / intention details
-- 
-- The following parts of the legacy system use intentions
--
-- -- case notes
--FC_GENERAL_NOTE
--
-- -- application update (a case note for the operator)
--FC_NOTE_FOR_OPERATOR
--FC_REVIEW_DECISION - severity: APPROVE 
--
-- -- technical review response (these are attached to the review intentions set rather than the main app intention set)
--FC_GENERAL_REVIEW_NOTE - General Review Note
--FC_UPDATE_REQUIRED - Update Required (Review Advise)
--FC_ISSUE_CONSENT - Issue Consent (Review Advise)
--
-- case decision/approval for issue
--FC_REVIEW_DECISION - severity: APPROVE 
--
-- case submission
--FC_SUBMISSION
--FC_SUBMISSION_APPROVAL
--FC_SUBMISSION_EMAIL
SELECT DISTINCT xid.class_type--, xid.clause_type, xid.status
FROM bpmmgr.xview_intention_details xid
WHERE xid.clause_type = 'FIELD_CONSENTS'
AND xid.end_datetime IS NULL
ORDER BY 1 --2, 3
/



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
--DONE flares

--DONE vent_annual_months
--DONE vent_short_term_months
--DONE vent_report_gas_data
--DONE vent_report_periods
--DONE vent_report_months
--DONE vents


-- CASE_PROCESSING TABLES
--DONE application_case_notes
--NA application_consultations (there are no external consultation in the legacy system)
--DONE application_updates
--DONE application_technical_reviews
--NA application_withdrawals (the operator doesn't submit a withdral request, but they can withdraw if they don't want to complete an app update)
--NA application_work_area_priorities

-- FILE UPLOADS?
--NA file_upload_library_flyway
--NA file_upload_library_shedlock
--IN PROGRESS file_upload_library_uploaded_files


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
--NA file_upload_library_uploaded_files_aud

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
  AND (ifcd.status, ifcd.version_status) NOT IN (
    ('INPROGRESS', 'PENDING') -- an unsubmitted application update (for any version/variation) (don't migrate)
  , ('INPROGRESS', 'CURRENT') -- an unsubmitted application (version 1 variation 0) (don't migrate)
  )
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

--SELECT wua.wua_id, xrph.full_name
--FROM securemgr.web_user_account_current wua
--JOIN decmgr.xview_resource_people_history xrph ON xrph.rp_id = wua.person_id AND xrph.status_control = 'C'
/

WITH stage_assignments AS (
  SELECT /*+ materialize */
    xbc.primary_data_uref fcd_uref
  , xa.assignee_uref
  , wua.id wua_id
  , wua.login_id
  , bs.stage_label
  , bs.end_datetime bs_end_datetime
  , CASE c.assignment
    WHEN 'FC_R10_DTI_ADMIN' THEN 'CAM'
    ELSE 'CASE_OFFICER'
    END assignment_role
  FROM bpmmgr.xview_business_contexts xbc
  JOIN bpmmgr.business_routine_contexts brc ON xbc.bc_id = brc.bc_id
  JOIN bpmmgr.business_stages bs ON brc.id = bs.brc_id --AND bs.end_datetime IS NULL
  JOIN bpmmgr.xview_bpd_stages xbpds ON xbpds.stage_label = bs.stage_label AND xbpds.bp_id = bs.bp_id AND xbpds.stage_classification = 'TOP-LEVEL'
  JOIN bpmmgr.business_processes bp ON bp.id = bs.bp_id
  JOIN bpmmgr.xview_bpd_stage_clocks c ON bs.stage_label = c.stage_label -- if a stage has multiple CLOCK assignments defined then this adds cardinality
  JOIN bpmmgr.business_routine_assignments bra ON bra.brc_id = bs.brc_id AND bra.assignment = c.assignment
  JOIN bpmmgr.xview_assignees xa ON xa.bas_id  = bra.bas_id AND xa.status_control = 'C' -- any additional cardinality from the clock join is removed here
  JOIN securemgr.web_user_accounts wua ON wua.id||'WUA' = xa.assignee_uref
  WHERE xbc.context_name IN ('FC_ROOT', 'OUTCOME_ACTIVITY', 'UPDATE')
  AND xbc.primary_data_uref LIKE '%FC'
  AND bp.short_name = 'FC_ADMIN'
  AND c.assignment IN ('FC_R6_DTI_ADMIN', 'FC_ADMINISTRATOR', 'FC_REVISION_ADMIN', 'FC_R10_DTI_ADMIN')
)
, ranked_stage_assignments AS (
  SELECT
    sa.*
  , RANK () OVER (PARTITION BY sa.fcd_uref, sa.assignment_role ORDER BY sa.bs_end_datetime DESC NULLS FIRST) rank_rownum
  FROM stage_assignments sa
)
, tip_assignments AS (
  SELECT
    rsa.*
  FROM ranked_stage_assignments rsa
  WHERE rsa.rank_rownum = 1
)
, t AS (
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
, (
    SELECT ta.wua_id
    FROM tip_assignments ta
    WHERE ta.fcd_uref = fcd.id||'FC'
    AND ta.assignment_role = 'CASE_OFFICER'
  ) case_officer_wua_id
, (
    SELECT ta.wua_id
    FROM tip_assignments ta
    WHERE ta.fcd_uref = fcd.id||'FC'
    AND ta.assignment_role = 'CAM'
  ) cam_wua_id
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
, coalesce(ltp.oil_min_value, 0) oil_min_value
, ltp.oil_max_value
, coalesce(ltp.gas_min_value, 0) gas_min_value
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
, coalesce(ap.oil_min_value, 0) oil_min_value
, ap.oil_max_value
, coalesce(ap.gas_min_value, 0) gas_min_value
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
  , coalesce(stp.oil_min_value, 0) oil_min_value
  , stp.oil_max_value
  , coalesce(stp.gas_min_value, 0) gas_min_value
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
  ed.*
, fcd.fc_id
, fcd.status
, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
AND ed.categories = 'A_B_C'
ORDER BY fcd.id, ed.ed_rownum;
/

--
-- flare_annual_123_months
--
SELECT
  ed.*
, fcd.fc_id
, fcd.status
, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
AND ed.categories = '1_2_3'
ORDER BY fcd.id, ed.ed_rownum;
/


--
-- flare_short_term_months
--

SELECT
  ed.fcd_id application_version_id
, ed.year
, ed.month
, ed.start_date
, ed.end_date
, ed.category_a
, ed.category_b
, ed.category_c
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_short_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
AND ed.categories = 'A_B_C'
ORDER BY ed.fcd_id, ed.ed_rownum;
/

--
-- flare_short_term_123_months
--

SELECT
  ed.fcd_id application_version_id
, ed.year
, ed.month
, ed.start_date
, ed.end_date
, ed.category_1
, ed.category_2
, ed.category_3
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_short_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
AND ed.categories = '1_2_3'
ORDER BY ed.fcd_id, ed.ed_rownum;
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
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
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
, base_av AS (
  SELECT b.application_version_id
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  b.application_version_id

, bd.category_a category_a_density
, bi.category_a category_a_inert_percentage
, bh.category_a category_a_hydro_percentage

, bd.category_b category_b_density
, bi.category_b category_b_inert_percentage
, bh.category_b category_b_hydro_percentage

, bd.category_c category_c_density
, bi.category_c category_c_inert_percentage
, bh.category_c category_c_hydro_percentage
FROM base_av b
LEFT JOIN base bd ON bd.application_version_id = b.application_version_id AND bd.data_type = 'DENSITY'
LEFT JOIN base bi ON bi.application_version_id = b.application_version_id AND bi.data_type = 'INERT'
LEFT JOIN base bh ON bh.application_version_id = b.application_version_id AND bh.data_type = 'HYDRO'
/

--
-- flare_report_123_gas_data
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
  , rd.category_1
  , rd.category_2
  , rd.category_3
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'INFO' 
  AND fcd.application_type = 'FCON'
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND coalesce(rd.category_1, rd.category_2, rd.category_3) IS NOT NULL
  ORDER BY rd.fcd_id, rd.rd_rownum
)
-- below required as we may be missing a row for any of the 3 data_types
-- so use this as the starting point for the pivot query below then use
-- LEFT joins for all there data_type sets
, base_av AS (
  SELECT b.application_version_id
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  b.application_version_id

, bd.category_1 category_1_density
, bi.category_1 category_1_inert_percentage
, bh.category_1 category_1_hydro_percentage

, bd.category_2 category_2_density
, bi.category_2 category_2_inert_percentage
, bh.category_2 category_2_hydro_percentage

, bd.category_3 category_3_density
, bi.category_3 category_3_inert_percentage
, bh.category_3 category_3_hydro_percentage
FROM base_av b
LEFT JOIN base bd ON bd.application_version_id = b.application_version_id AND bd.data_type = 'DENSITY'
LEFT JOIN base bi ON bi.application_version_id = b.application_version_id AND bi.data_type = 'INERT'
LEFT JOIN base bh ON bh.application_version_id = b.application_version_id AND bh.data_type = 'HYDRO'
--SELECT b.application_version_id, st.join(stagg(b.data_type))
--FROM base b
--GROUP BY b.application_version_id
----HAVING st.join(stagg(b.data_type)) NOT LIKE '%DENSITY%'
----HAVING st.join(stagg(b.data_type)) NOT LIKE '%INERT%'
----HAVING st.join(stagg(b.data_type)) NOT LIKE '%HYDRO%'
/

SELECT ap.*
FROM fcs_migration.application_versions av
JOIN fcs_migration.applications ap ON ap.id = av.application_id
--WHERE av.id IN (1799, 1802, 2280, 3283, 3284, 3520, 3522, 3523) -- missing DENSITY row
WHERE av.id IN (3265, 3311, 3316, 3393, 3401, 3465, 808, 908) -- missing INERT row
--WHERE av.id IN (2695, 2696, 3012, 3013, 3014) -- missing HYDRO row
ORDER BY ap.fc_id
/
--These app numbers are good examples with missing data for all three categories for a data type
-- DENSITY (Stream Mol Wt) 913, 1298, 1658, 1727
-- INERT (Inert Gas Content) 417, 1173, 1520, 1527, 1531
-- HYDRO (Hydrocarbon content) 1204, 1205, 1477, 1478

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
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
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
-- 1_2_3 report periods go into the same table as A_B_C
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , rd.rd_rownum
  , CASE
    WHEN rd.rd_month IS NULL THEN
      last_day(to_date('01-'||rd.upper_desc||'-'||to_char(xfcd.application_year - 1), 'DD-MONTH-YYYY'))
    ELSE
      last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY'))
    END report_row_end_date
  , xfcd.application_year
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'FCON'
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND coalesce(rd.category_1, rd.category_2, rd.category_3, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
, report_end AS (
  SELECT b.application_version_id, max(b.report_row_end_date) report_row_end_date
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;
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
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
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

--
-- flare_report_123_months
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , xfcd.application_year
  , xfcd.app_length
  , rd.rd_month
  , CASE
    WHEN rd.rd_month IS NULL THEN
      last_day(to_date('01-'||rd.upper_desc||'-'||to_char(xfcd.application_year - 1), 'DD-MONTH-YYYY'))
    ELSE
      last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY'))
    END report_row_end_date
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
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND coalesce(rd.category_1, rd.category_2, rd.category_3, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
SELECT
  b.application_version_id
, to_number(to_char(b.report_row_end_date, 'YYYY')) year
, to_char(b.report_row_end_date, 'MONTH') month
, b.category_1
, b.category_2
, b.category_3
, b.days_total_shutdown
, b.comments
FROM base b;
/

--
-- flares
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT flares_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, flare_no               INTEGER NOT NULL
--, flare_type             VARCHAR2(4000) NOT NULL
--, description            VARCHAR2(4000)
--, metered_flag           VARCHAR2(5) NOT NULL
--, comments               VARCHAR2(4000)

SELECT
  fcd.id application_version_id
, RANK () OVER (PARTITION BY fcd.id ORDER BY es.es_rownum) flare_no
, es.type flare_type
, es.description
, es.metered metered_flag
, es.comments
--, fcd.fc_id
--, fcd.application_type
--, fcd.status
--, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_emission_systems es ON es.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
ORDER BY av.id ASC;
/

--
-- vent_annual_months
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT vent_annual_months_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, year                   INTEGER NOT NULL
--, month                  VARCHAR2(4000) NOT NULL
--, category_a             NUMBER NOT NULL
--, category_b             NUMBER NOT NULL
--, category_c             NUMBER NOT NULL
--, comments               VARCHAR2(4000)

SELECT
  ed.*
, fcd.fc_id
, fcd.status
, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = 'A_B_C'
ORDER BY fcd.id, ed.ed_rownum;
/
--
-- vent_annual_123_months
--
SELECT
  ed.*
, fcd.fc_id
, fcd.status
, fcd.version_status
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = '1_2_3'
-- category_1 data is null for the legacy terminal apps (i.e. there no consent months data so don't migrate)
AND ed.category_1 IS NOT NULL
ORDER BY fcd.id, ed.ed_rownum;
/


--
-- vent_short_term_months
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT vent_short_term_months_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, year                   INTEGER NOT NULL
--, month                  VARCHAR2(4000) NOT NULL
--, start_date             DATE
--, end_date               DATE
--, category_a             NUMBER NOT NULL
--, category_b             NUMBER NOT NULL
--, category_c             NUMBER NOT NULL
--, comments               VARCHAR2(4000)

SELECT
  ed.fcd_id application_version_id
, ed.year
, ed.month
, ed.start_date
, ed.end_date
, ed.category_a
, ed.category_b
, ed.category_c
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_short_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = 'A_B_C'
ORDER BY fcd.id, ed.ed_rownum;
/

--
-- vent_short_term_123_months
--

SELECT
  ed.fcd_id application_version_id
, ed.year
, ed.month
, ed.start_date
, ed.end_date
, ed.category_1
, ed.category_a
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_short_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = '1_2_3'
-- category_1 data is null for the legacy terminal apps (i.e. there no consent months data so don't migrate)
AND ed.category_1 IS NOT NULL
ORDER BY fcd.id, ed.ed_rownum;
/ 


--
-- vent_report_gas_data
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , CASE
    WHEN rd.upper_desc LIKE '%STREAM%' THEN 'DENSITY'
    WHEN rd.upper_desc LIKE '%INERT%' THEN 'INERT'
    WHEN rd.upper_desc LIKE '%HYDROCARBON%' THEN 'HYDRO'
    END data_type
  , rd.category_a
  , rd.category_b
  , rd.category_c
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'INFO' 
  AND fcd.application_type = 'VCON'
  AND rd.categories = 'A_B_C'
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
  AND coalesce(rd.category_a, rd.category_b, rd.category_c) IS NOT NULL
  ORDER BY rd.fcd_id, rd.rd_rownum
)
-- below required as we may be missing a row for any of the 3 data_types
-- so use this as the starting point for the pivot query below then use
-- LEFT joins for all there data_type sets
, base_av AS (
  SELECT b.application_version_id
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  b.application_version_id
, bd.category_a category_a_density
, bi.category_a category_a_inert_percentage
, bh.category_a category_a_hydro_percentage
, bd.category_b category_b_density
, bi.category_b category_b_inert_percentage
, bh.category_b category_b_hydro_percentage
, bd.category_c category_c_density
, bi.category_c category_c_inert_percentage
, bh.category_c category_c_hydro_percentage
, null evaluated_per_category
, null evaluated_per_category_explanation
FROM base_av b
LEFT JOIN base bd ON bd.application_version_id = b.application_version_id AND bd.data_type = 'DENSITY'
LEFT JOIN base bi ON bi.application_version_id = b.application_version_id AND bi.data_type = 'INERT'
LEFT JOIN base bh ON bh.application_version_id = b.application_version_id AND bh.data_type = 'HYDRO'
/

--
-- vent_report_123_gas_data
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
  , rd.category_1
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'INFO' 
  AND fcd.application_type = 'VCON'
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND rd.category_1 IS NOT NULL
  ORDER BY rd.fcd_id, rd.rd_rownum
)
-- below required as we may be missing a row for any of the 3 data_types
-- so use this as the starting point for the pivot query below then use
-- LEFT joins for all there data_type sets
, base_av AS (
  SELECT b.application_version_id
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  b.application_version_id

, bd.category_1 category_1_density
, bi.category_1 category_1_inert_percentage
, bh.category_1 category_1_hydro_percentage
FROM base_av b
LEFT JOIN base bd ON bd.application_version_id = b.application_version_id AND bd.data_type = 'DENSITY'
LEFT JOIN base bi ON bi.application_version_id = b.application_version_id AND bi.data_type = 'INERT'
LEFT JOIN base bh ON bh.application_version_id = b.application_version_id AND bh.data_type = 'HYDRO'
/

--
-- vent_report_periods
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT vent_report_periods_fk1_av_id
--                         REFERENCES fcs_migration.application_versions
--, report_end_month       VARCHAR2(4000) NOT NULL
--, report_end_year        INTEGER NOT NULL

WITH base AS (
  SELECT
    fcd.id application_version_id
  , rd.rd_rownum
  , last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY')) report_row_end_date
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'VCON'
  AND rd.categories = 'A_B_C'
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
  AND coalesce(rd.category_a, rd.category_b, rd.category_c, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
, report_end AS (
  SELECT b.application_version_id, max(b.report_row_end_date) report_row_end_date
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;

--
-- 1_2_3 report periods go into the same table as A_B_C
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , rd.rd_rownum
  , CASE
    WHEN rd.rd_month IS NULL THEN
      last_day(to_date('01-'||rd.upper_desc||'-'||to_char(xfcd.application_year - 1), 'DD-MONTH-YYYY'))
    ELSE
      last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY'))
    END report_row_end_date
  , xfcd.application_year
  , rd.rd_month
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'VCON'
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND coalesce(rd.category_1, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
, report_end AS (
  SELECT b.application_version_id, max(b.report_row_end_date) report_row_end_date
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;
/

--
-- vent_report_months
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY')) report_row_end_date
  , rd.*
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'VCON'
  AND rd.categories = 'A_B_C'
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
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

--
-- vent_report_123_months
--
WITH base AS (
  SELECT
    fcd.id application_version_id
  , CASE
    WHEN rd.rd_month IS NULL THEN
      last_day(to_date('01-'||rd.upper_desc||'-'||to_char(xfcd.application_year - 1), 'DD-MONTH-YYYY'))
    ELSE
      last_day(to_date('01-'||rd.rd_month, 'DD-MON-YYYY'))
    END report_row_end_date
  , rd.*
  FROM fcs_migration.application_versions av
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  JOIN fcs_migration.field_consent_report_data rd ON rd.fcd_id = fcd.id
  WHERE rd.rd_type = 'MONTH' 
  AND fcd.application_type = 'VCON'
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND coalesce(rd.category_1, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
SELECT
  null-- fcs_migration.vent_report_123_month_id_seq.nextval id
, b.application_version_id
, to_number(to_char(b.report_row_end_date, 'YYYY')) year
, to_char(b.report_row_end_date, 'MONTH') month
, coalesce(b.category_1, 0) category_1
, b.days_total_shutdown
, b.comments
FROM base b;
/

SELECT ap.*, av.id, av.version_no
FROM fcs_migration.application_versions av
JOIN fcs_migration.applications ap ON ap.id = av.application_id
--WHERE av.id IN (358, 4224, 3947, 3956, 3970, 4019)
--WHERE av.id IN (3787, 3788, 1319, 4256, 4181, 3944, 4253, 4260, 3934, 3948)
WHERE av.id IN (59, 4254, 4256)
ORDER BY ap.fc_id
/



--
-- vents
--
SELECT
  fcd.id application_version_id
, RANK () OVER (PARTITION BY fcd.id ORDER BY es.es_rownum) vent_no
, es.type vent_type
, es.description
, es.metered metered_flag
, es.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_emission_systems es ON es.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON';
/

--
-- case_notes
--

--  id                     INTEGER PRIMARY KEY
--, application_version_id INTEGER NOT NULL
--                         CONSTRAINT app_case_notes_fk1_version_id
--                         REFERENCES fcs_migration.application_versions
--, added_by_wua_id        INTEGER NOT NULL
--, added_date_time        DATE NOT NULL
--, case_note_text         CLOB NOT NULL

-- Just convert html to text for now.

-- When a new app version is created all the original intentions are cloned into the
-- new version (under a new intention set) as you can't see the old version after an
-- update is submitted. This means that really for a particular version we only
-- want to migrate the original intentions else we'll have duplicates within
-- a variation.
--
-- For new variations we always start with a blank set of intentions.
--
-- NOTE - have spotted that for regulator initiated revisions (i.e. just a clerical
-- error reissue, after the new intention set is created the system fails to update the
-- intention_set_id in the case DOM /*/INTENTION_SET_ID. This doesn't appear to be being
-- used anywhere. So instead of looking at that data we should get the intention_set_id
-- from via the primary_data_uref (fcd_id||'FC')

-- Good test case on dev FCON/2033/0

SELECT
  fci.fcd_id application_version_id
, fci.created_by_wua_id
, fci.created_datetime
, fci.intention_text case_note_text
, fci.*
FROM fcs_migration.application_versions av
JOIN fcs_migration.field_consent_intentions fci ON fci.fcd_id = av.id
WHERE fci.class_type = 'FC_GENERAL_NOTE'
ORDER BY fci.in_id DESC, fci.id_id DESC
/

--SELECT xis.*, xfcd.intention_set_id, xfcd.*
--FROM bpmmgr.xview_intention_sets xis
--JOIN envmgr.field_consent_details fcd ON fcd.id||'FC' = xis.primary_data_uref
--JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
----JOIN bpmmgr.xview_intention_sets xis2 ON xis2.primary_data_uref = xis.primary_data_uref AND xis2.is_id != xis.is_id
--WHERE xis.primary_data_uref LIKE '%FC'
--AND xis.is_id != xfcd.intention_set_id
--/
--
--SELECT xfcd.*
--FROM envmgr.xview_field_consent_details xfcd
--WHERE xfcd.fc_id IN (214, 1024, 1711, 1023, 1879, 1996, 1988, 1857, 1807, 2013)
--ORDER BY xfcd.fc_id, xfcd.fcd_id
--/

--SELECT xfcd.*
--FROM envmgr.field_consent_details fcd
--JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
--WHERE fcd.fc_id = 6490
--/
---- intention_set_id IN (185804, 185657)
--SELECT TISI.IS_ID, tin.*, xid.*
--, st.html_to_string(xid.clause_text) case_note_text
--FROM bpmmgr.xview_intention_details xid
--JOIN bpmmgr.intentions tin ON tin.id = xid.in_id
--JOIN bpmmgr.intention_set_intentions tisi ON tisi.in_id = tin.id
--WHERE xid.clause_type = 'FIELD_CONSENTS'
----AND xid.class_type = 'FC_GENERAL_NOTE'
--AND xid.end_datetime IS NULL
--AND TISI.IS_ID IN (185804, 185657)
--ORDER BY xid.in_id DESC, xid.id_id DESC
--/
--
--SELECT tisi.*, tin.*, xid.*
--FROM bpmmgr.intention_set_intentions tisi
--JOIN bpmmgr.intentions tin ON tin.id = tisi.in_id
--JOIN bpmmgr.xview_intention_details xid ON xid.in_id = tin.id
--WHERE xid.clause_type = 'FIELD_CONSENTS'
----AND tin.original_id_id IS NOT NULL
--AND xid.end_datetime IS NULL
----AND XID.STATUS != 'CLOSED'
--AND (tisi.is_id IN (200726, 200634) OR XID.ID_ID = 465145)
--ORDER BY XID.IN_ID DESC
/
--
--SELECT
----  tisi.*
----, OXID.STATUS
----, oxid.clause_text, xid.clause_text
----, XID.STATUS
----, XMLDIFF(oxid.clause_text, xid.clause_text)
--1
--FROM bpmmgr.intention_set_intentions tisi
--JOIN bpmmgr.intentions tin ON tin.id = tisi.in_id
--JOIN bpmmgr.xview_intention_details xid ON xid.in_id = tin.id
--JOIN bpmmgr.xview_intention_details txid ON txid.id_id = tin.original_id_id
--WHERE xid.clause_type = 'FIELD_CONSENTS'
--AND xid.end_datetime IS NULL
--AND tin.original_id_id IS NOT NULL
--AND dbms_lob.compare(txid.clause_text.getClobVal(), xid.clause_text.getClobVal()) != 0
--/

--SELECT fci.*
--FROM fcs_migration.field_consent_intentions fci
--ORDER BY fci.id_id DESC
--/
--SELECT
----  fcd.id fcd_id
----, 
--  xis.*
--, xid.in_id
--, xid.id_id
--, xid.created_by_wua_id
--, xid.created_datetime
--, xid.clause_type
--, xid.class_type
--, xid.severity
--, st.html_to_string(xid.clause_text) intention_text
--FROM bpmmgr.xview_intention_sets xis
--JOIN bpmmgr.intention_set_intentions isi ON isi.is_id = xis.is_id AND isi.end_datetime IS NULL
--JOIN bpmmgr.intentions i ON i.id = isi.in_id
--JOIN bpmmgr.xview_intention_details xid ON xid.in_id = i.id
--WHERE --xid.clause_type = 'FIELD_CONSENTS'
----AND 
--xid.end_datetime IS NULL
---- remove the duplicates across the sets (for the app versions within a variation)
--AND i.original_id_id IS NULL
--ORDER BY xid.id_id DESC

--
-- application_updates
--

--  id                              INTEGER PRIMARY KEY
--, application_version_id          INTEGER NOT NULL
--                                  CONSTRAINT app_updates_fk1_version_id
--                                  REFERENCES fcs_migration.application_versions
--, requested_by_wua_id             INTEGER NOT NULL
--, requested_date_time             DATE NOT NULL
--, request_text                    CLOB NOT NULL
--, deadline_date_time              DATE NOT NULL -- might need to allow NULLs here
--, responded_by_wua_id             INTEGER
--, responded_date_time             DATE
--, response_text                   CLOB
--, response_type                   VARCHAR2(4000)
--, application_update_status       VARCHAR2(4000) NOT NULL
--, response_application_version_id INTEGER
--                                  CONSTRAINT app_updates_fk2_resp_version_id
--                                  REFERENCES fcs_migration.application_versions

SELECT
  av.id application_version_id
, av.version_no
, avnext.version_no
, fci.created_by_wua_id requested_by_wua_id
, fci.created_datetime requested_date_time
, fci.intention_text request_text
, fci.created_datetime deadline_date_time -- TODO set as the request date or leave null?
, avnext.submitted_by_wua_id responded_by_wua_id
, avnext.submitted_date_time responded_date_time
, null response_text
, null response_type
, 'CLOSED' application_update_status -- TODO best to mark all closed I think
, avnext.id response_application_version_id
--a.*, av.*
--, avnext.*
--  count(*)
FROM fcs_migration.application_versions av
LEFT JOIN fcs_migration.application_versions avnext ON avnext.id = (
--avnext.application_id = av.application_id AND avnext.version_no = av.version_no + 1  AND avnext.status != 'DELETED' AND avnext.created_date_time > av.created_date_time
  SELECT min(av2.id) -- if a variation is subsequently withdraw they could start a variation again (got same number) and therefore have more that one next version within the variations set of versions (see fc_id = 1384 on dev)
  FROM fcs_migration.application_versions av2
  WHERE av2.application_id = av.application_id
  AND av2.version_no = av.version_no + 1
  AND av2.status != 'DELETED'
  AND av2.created_date_time > av.created_date_time
)
JOIN fcs_migration.field_consent_intentions fci ON fci.fcd_id = av.id
JOIN applications a ON a.id = av.application_id
WHERE (fci.class_type, fci.severity) IN (
  ('FC_NOTE_FOR_OPERATOR', 'NONE')
, ('FC_REVIEW_DECISION', 'REQUEST_UPDATE')
)

--WHERE --av.application_id != 4115
--AND av.status != 'DELETED'
--AND 
--avnext.id IS NOT NULL
ORDER BY av.application_id, av.id
/

SELECT xfcd.fcd_id, xfcd.status, xfcd.version_status, av.*
FROM envmgr.xview_field_consent_details xfcd
LEFT JOIN fcs_migration.application_versions av ON av.id = xfcd.fcd_id
WHERE xfcd.fc_id = 1384
ORDER BY xfcd.created_date 
/
--3850
--3851
--3853
--3855
--3856

--
-- application_technical_reviews
--

SELECT
  rid.xml_data rid_xml_data
, at.xml_data at_xml_data
, rid.id review_invitation_detail_id
FROM appenv.review_invitations ri
JOIN appenv.review_invitation_details rid ON ri.id = rid.ri_id AND rid.status_control = 'C'
JOIN appenv.advice_types at ON ri.advice_type = at.advice_type
WHERE ri.id = 26537

/




WITH ri AS (
  SELECT d.*
  FROM bpmmgr.xview_review_inv_details d
  
  WHERE d.status_control = 'C'
  AND d.primary_data_uref = '4326FC'
)
SELECT
  ri.advice_type
, rt.review_type
, rt.review_title
, rt.method_type
, rt.method_title
, rt.batch_availability
, rt.individual_allow_late_reviews
, rt.slot_configuration
, rt.review_messaging
, rt.review_deadline
--, CASE
--    WHEN rt.review_deadline = 'true' AND rt.review_run_default_days IS NOT NULL
--      THEN TO_CHAR(bpmmgr.clock.compute_timing(SYSDATE, rt.review_run_default_days + 1), 'YYYY-MM-DD')
--    ELSE NULL
--  END review_deadline_date
, rt.aac_pickable_flag
, rt.aac_pick_default_recommended
FROM appenv.review_invitations ri
JOIN appenv.xview_review_types rt ON ri.advice_type = rt.advice_type
WHERE ri.id = (SELECT ri.ri_id FROM ri)
--AND rt.review_type = 'FULL'
/

WITH isetins AS (
  SELECT isi.is_id
--  , xtcd.title||': '||st.html_to_string(xid.clause_text) response_text
  , '<p>'||xtcd.title||'</p>'||XMLQUERY('/CLAUSE_TEXT/node()' PASSING xid.clause_text RETURNING CONTENT).getClobVal() response_text
  FROM bpmmgr.review_advisor_slot_details rasd
  JOIN bpmmgr.xview_intention_sets xis ON xis.is_id = rasd.intention_set_id
  JOIN bpmmgr.intention_set_intentions isi ON isi.is_id = xis.is_id AND isi.end_datetime IS NULL
  JOIN bpmmgr.intentions i ON i.id = isi.in_id
  JOIN bpmmgr.xview_intention_details xid ON xid.in_id = i.id AND xid.end_datetime IS NULL
  JOIN bpmmgr.xview_template_clause_details xtcd ON xtcd.clause_type_id = xid.clause_type AND xtcd.class = xid.class_type
  WHERE upper(rasd.name) LIKE '%FIELD CONSENT REVIEW%'
  AND xis.domain = 'RESPONSE'
  AND xis.primary_data_uref = 'PSUEDO_SLOT_MSD'
  --AND xp.get_root_name(xid.clause_text) != 'CLAUSE_TEXT'
  ORDER BY isi.is_id, xid.created_datetime
)
, isets AS (
  SELECT i.is_id
  , st.joinclob(staggclob(i.response_text), '<br/><br/>') response_text
  FROM isetins i
  GROUP BY i.is_id
)
, aac_mems AS (
  SELECT
    aac.id aac_id
  , rmc.resource_person_id
  , RANK () OVER (PARTITION BY aac.id ORDER BY rmc.start_date DESC) aac_mem_rank
  FROM bpmmgr.advice_advisory_communities aac
  JOIN decmgr.resource_usages_current ru ON ru.uref = aac.id||'AAC' 
  JOIN decmgr.xview_resource_members_history rmc ON rmc.res_id = ru.res_id AND rmc.role_name = 'ELECTRONIC_ADVISOR_AUTO'
  WHERE aac.advice_type = 'FIELD_CONSENTS'
)
, aac_wuas AS (
  SELECT
    m.aac_id
  , m.resource_person_id
  , (
    SELECT max(wua.wua_id) -- this is a bit wierd but will return us the latest created wua for the person
    FROM securemgr.web_user_account_current wua
    WHERE wua.person_id = m.resource_person_id
    ) wua_id
  FROM aac_mems m
  WHERE m.aac_mem_rank = 1 -- we grab the last member added to the aac team ELECTRONIC_ADVISOR_AUTO role (only 1 member of this role for legacy field consents)
)
SELECT
--  rid.primary_data_uref
--, rid.ri_id
--, rr.id rr_id
--, xrad.ra_id
--, aac.id aac_id
----, ri.*
----, rt.*
----, at.*
----, rr.*
----, rrd.*
----, xrrd.*
--, rreq.*
----, rreqd.*
--, xrreqd.review_dispatched_date
--, aac.*
--, ac.*
--, ab.*
--, ra.*
--, xrad.*
  (
  SELECT av2.id -- the latest app version at the time of the review
  FROM fcs_migration.application_versions av2
  WHERE av2.application_id = av.application_id
  AND xrad.review_delivered_date > av2.created_date_time
  AND av2.status != 'DELETED'
  ORDER BY av2.created_date_time DESC
  FETCH FIRST 1 ROWS ONLY
  ) request_application_version_id
, rreqd.created_by_wua_id requested_by_wua_id
, (SELECT w.login_id FROM securemgr.web_user_accounts w WHERE w.id = rreqd.created_by_wua_id) req_user
, xrad.review_delivered_date requested_date_time
, NULL request_text
, xrad.review_deadline_date deadline_date_time
, ac.name

, aac_wuas.*

, coalesce(aac_wuas.wua_id, rasd.status_by_wua_id) technical_reviewer_wua_id
, (SELECT w.login_id FROM securemgr.web_user_accounts w WHERE w.id = coalesce(aac_wuas.wua_id, rasd.status_by_wua_id)) tech_user
, rasd.status_by_wua_id response_wua_id
, coalesce(xrad.review_completed_date, xrad.review_closed_date) responded_date_time
, isets.response_text
, CASE rasd.response_decision
  WHEN 'ISSUE_CONSENT' THEN 'APPROVE'
  WHEN 'UPDATE_REQUIRED' THEN 'REJECT'
  ELSE NULL
  END response_type
, CASE
  WHEN xrad.status IN ('COMPLETED', 'CLOSED')  THEN 'CLOSED'
  ELSE 'OPEN'
  END technical_review_status
, (
  SELECT av2.id -- the latest app version at the time of the response
  FROM fcs_migration.application_versions av2
  WHERE av2.application_id = av.application_id
  AND coalesce(xrad.review_completed_date, xrad.review_closed_date) IS NOT NULL
  AND coalesce(xrad.review_completed_date, xrad.review_closed_date) > av2.created_date_time
  AND av2.status != 'DELETED'
  ORDER BY av2.created_date_time DESC
  FETCH FIRST 1 ROWS ONLY
  ) response_application_version_id
  
--, xrad.status rad_status
--, xrad.review_closed_date
--, xrad.review_completed_date
--, rasd.status slot_status
--, rasd.response_decision
--, wua.full_name||' ('||to_char(rasd.status_date, 'DD-MON-YYYY HH24:Mi:SS')||')' response_info
--, isets.*
--, ru.*
--, rmc.*
FROM fcs_migration.application_versions av
--JOIN envmgr.xview_field_consent_details fcd ON fcd.fcd_id = av.id
JOIN bpmmgr.xview_review_inv_details rid ON rid.primary_data_uref = av.id||'FC'
--JOIN bpmmgr.review_invitations ri ON ri.id = rid.ri_id
--JOIN bpmmgr.xview_review_types rt ON ri.advice_type = rt.advice_type
--JOIN bpmmgr.advice_types at ON ri.advice_type = at.advice_type
JOIN bpmmgr.review_runs rr ON rr.ri_id = rid.ri_id
--JOIN bpmmgr.review_run_details rrd ON rrd.rrun_id = rr.id AND rrd.status_control = 'C'
JOIN bpmmgr.xview_review_run_details xrrd ON xrrd.rrun_id = rr.id AND xrrd.status_control = 'C'
JOIN bpmmgr.review_requests rreq ON rreq.rrun_id = rr.id
JOIN bpmmgr.review_request_details rreqd ON rreqd.rreq_id = rreq.id AND rreqd.status_control = 'C'
JOIN bpmmgr.xview_review_request_details xrreqd ON xrreqd.rreq_id = rreq.id AND xrreqd.status_control = 'C'
JOIN bpmmgr.advice_advisory_communities aac ON rreq.aac_id = aac.id
JOIN bpmmgr.advisory_communities ac ON aac.ac_id = ac.id AND aac.ab_id = ac.ab_id
JOIN bpmmgr.advisory_bodies ab ON aac.ab_id = ab.id
JOIN bpmmgr.review_advisors ra ON ra.rreq_id = rreq.id
JOIN bpmmgr.review_advisor_details rad ON ra.id = rad.ra_id AND rad.status_control = 'C'
JOIN bpmmgr.xview_review_advisor_details xrad ON xrad.ra_id = rad.ra_id AND xrad.status_control = 'C'
JOIN bpmmgr.review_advisor_slots ras ON ras.ra_id = ra.id
JOIN bpmmgr.review_advisor_slot_details rasd ON ras.id = rasd.ras_id AND rasd.status_control = 'C'
LEFT JOIN isets ON isets.is_id = rasd.intention_set_id
LEFT JOIN aac_wuas ON aac_wuas.aac_id = aac.id
--JOIN decmgr.resource_usages_current ru ON ru.uref = aac.id||'AAC' -- AND (xrad.review_delivered_date BETWEEN ru.start_datetime AND coalesce(ru.end_datetime, sysdate))
--JOIN decmgr.resource_member_current_simple rmc ON rmc.res_id = ru.res_id AND rmc.role_name = 'ELECTRONIC_ADVISOR_AUTO' -- TODO this could add cardinality - check that all the field consents aac team have just one ELECTRONIC_ADVISOR_AUTO on live / dev
WHERE rid.status_control = 'C'
-- this is version 4 what happens when we do an update and get version 5? do the reviews get copied forward / repointed to the new FC uref (this is the detail id!) ?
-- ah, the uref for the rid gets repointed at the new uref! so we loose the context of which app version the review was requested on ummmmm TODO need to think about this 
--AND rid.primary_data_uref = '4326FC'
ORDER BY av.id DESC, xrad.review_delivered_date DESC
/
SELECT DISTINCT xrad.status
FROM bpmmgr.xview_review_advisor_details xrad
/

-- ra_id 20053 20054
WITH ints AS (
  SELECT isi.is_id
  , st.joinclob(stagg(xtcd.title||': '||st.html_to_string(xid.clause_text)), CHR(10), NULL, NULL, NULL, 'false', 'ORDER BY 1 ASC') response_text
  FROM bpmmgr.xview_intention_sets xis
  JOIN bpmmgr.intention_set_intentions isi ON isi.is_id = xis.is_id AND isi.end_datetime IS NULL
  JOIN bpmmgr.intentions i ON i.id = isi.in_id
  JOIN bpmmgr.xview_intention_details xid ON xid.in_id = i.id AND xid.end_datetime IS NULL
  JOIN bpmmgr.xview_template_clause_details xtcd ON xtcd.clause_type_id = xid.clause_type AND xtcd.class = xid.class_type
  WHERE xis.is_id = 22995
  GROUP BY isi.is_id
)
SELECT
  rasd.status slot_status
, rasd.response_decision
, wua.response_wua_id
, wua.full_name||' ('||to_char(rasd.status_date, 'DD-MON-YYYY HH24:Mi:SS')||')' response_info
, rasd.uref slot_uref
, rasd.id slot_detail_id
--, (SELECT NVL2(psd.response_decision, NVL(xrtr.response_long_key, psd.response_decision) || DECODE(psd.status, 'COMPLETED', ' (Submitted)', ' (Not submitted)'), 'None' )
--   FROM previous_slot_decisions psd
--   LEFT JOIN bpmmgr.xview_review_type_responses xrtr ON psd.response_decision = xrtr.response_data AND xrtr.review_type = :review_type AND xrtr.advice_type = :advice_type
--   WHERE psd.ras_rank = 1
--   AND psd.ras_id = ras.id) prev_decision_in_current_rrun
, rasd.ff_id
, 'false' ff_active
, rasd.*
, ras.*
--, i.*
, xid.*
FROM bpmmgr.review_advisor_slots ras
JOIN bpmmgr.review_advisor_slot_details rasd ON ras.id = rasd.ras_id AND rasd.status_control = 'C'
LEFT JOIN securemgr.web_user_accounts wua ON rasd.status_by_wua_id = wua.id
LEFT JOIN bpmmgr.xview_intention_sets xis ON xis.is_id = rasd.intention_set_id
LEFT JOIN bpmmgr.intention_set_intentions isi ON isi.is_id = xis.is_id AND isi.end_datetime IS NULL
LEFT JOIN bpmmgr.intentions i ON i.id = isi.in_id
LEFT JOIN bpmmgr.xview_intention_details xid ON xid.in_id = i.id AND xid.end_datetime IS NULL
WHERE ras.ra_id IN (20053, 20054)

--WHERE xid.clause_type = 'FIELD_CONSENTS'
-- remove the duplicates across the sets (for the app versions within a variation)
--AND i.original_id_id IS NULL;


ORDER BY lower(rasd.name)
/


SELECT ac.id ac_id, ac.name, ac.status, aac.id aac_id
, mh.*
--, rmc.*
FROM bpmmgr.advisory_bodies ab
JOIN bpmmgr.advisory_communities ac ON ac.ab_id = ab.id
JOIN bpmmgr.advice_advisory_communities aac ON aac.ac_id = ac.id
JOIN decmgr.resource_usages_current ru ON ru.uref = aac.id||'AAC' -- AND (xrad.review_delivered_date BETWEEN ru.start_datetime AND coalesce(ru.end_datetime, sysdate))
LEFT JOIN decmgr.xview_resource_members_history mh ON mh.res_id = ru.res_id AND mh.role_name = 'ELECTRONIC_ADVISOR_AUTO' AND mh.status_control = 'C'
--LEFT JOIN decmgr.resource_member_current_simple rmc ON rmc.res_id = ru.res_id AND rmc.role_name = 'ELECTRONIC_ADVISOR_AUTO' -- TODO this could add cardinality - check that all the field consents aac team have just one ELECTRONIC_ADVISOR_AUTO on live / dev
WHERE aac.advice_type = 'FIELD_CONSENTS'
--GROUP BY aac.id
ORDER BY aac.id

/


SELECT wuah.*
FROM securemgr.web_user_account_histories wuah
WHERE wuah.resource_person_id IN (51747, 53827)
/

SELECT rp.*, wuah.*
FROM decmgr.xview_resource_people_history rp
LEFT JOIN securemgr.web_user_account_histories wuah ON wuah.resource_person_id = rp.rp_id
WHERE rp.rp_id IN (51747, 53827)
--WHERE upper(rp.forename) = 'ALISON'
--AND upper(rp.surname) like '%ARCY%'
--AND rp.status_control = 'C'
/
-- AAC/AC to wua check
-- This looks good all environments
-- dev
-- st
-- uat (had to fix up the "RE - Mike Hannan" ACC team)
-- live (had to fix up the "RE - Mike Hannan" ACC team)
WITH aac_mems AS (
  SELECT
    aac.id aac_id
  , ac.name ac_name
  , rmc.resource_person_id
  , RANK () OVER (PARTITION BY aac.id ORDER BY rmc.start_date DESC) aac_mem_rank
  FROM bpmmgr.advice_advisory_communities aac
  JOIN bpmmgr.advisory_communities ac ON ac.id = aac.ac_id
  JOIN decmgr.resource_usages_current ru ON ru.uref = aac.id||'AAC' 
  JOIN decmgr.xview_resource_members_history rmc ON rmc.res_id = ru.res_id AND rmc.role_name = 'ELECTRONIC_ADVISOR_AUTO'
  WHERE aac.advice_type = 'FIELD_CONSENTS'
)
, aac_wuas AS (
  SELECT
    m.aac_id
  , m.ac_name
  , m.resource_person_id
  , (
    SELECT max(wua.wua_id) -- this is a bit wierd but will return us the latest created wua for the person
    FROM securemgr.web_user_account_current wua
    WHERE wua.person_id = m.resource_person_id
    ) wua_id
  FROM aac_mems m
  WHERE m.aac_mem_rank = 1 -- we grab the last member added to the aac team ELECTRONIC_ADVISOR_AUTO role (only 1 member of this role for legacy field consents)
)
SELECT
  aw.*
, wua.full_name
, wua.primary_email_address
FROM aac_wuas aw
JOIN securemgr.web_user_accounts wua ON wua.id = aw.wua_id
/
SELECT *
FROM fcs_migration.application_technical_reviews
ORDER BY requested_date_time DESC
/
SELECT *
FROM fcs_migration.application_case_notes
ORDER BY added_date_time DESC
/
SELECT *
FROM fcs_migration.application_updates
ORDER BY requested_date_time DESC
/

--
-- file_upload_library_uploaded_files
--
-- supporting info docs
--

-- note - as per the new system the files are cloned to a new file folder for
-- an application update, i.e. new application version

SELECT count(*)
, sum(CASE WHEN ff.id IS NULL THEN 1 ELSE 0 END) missing_folder_count
, sum(CASE WHEN ffu.ff_id IS NULL THEN 1 ELSE 0 END) missing_folder_usage_count
FROM envmgr.xview_field_consent_details fcd
LEFT JOIN decmgr.file_folders ff ON ff.id = fcd.folder_id
LEFT JOIN decmgr.file_folder_usages ffu ON ffu.uref = fcd.uref;

/

--
-- File migration proposed method
--
-- 1) insert data we have into fcs_migration.file_upload_library_uploaded_files (will be missing the id and key)
-- 2) insert data required (blobs etc) into promotemgr.s3_file_migration
-- 3) run the Java file migration tool (from the bastion for uat and prod)
--    -- generate the uuid key (stored at text in FUSS) here? or at step (2)?
-- 4) get the key from promotemgr.s3_file_migration and update fcs_migration.file_upload_library_uploaded_files
-- 5) push the data from Oracle to Postgress
