--
-- Run script with F5 in Toad
--
-- Full execution run times:
-- UAT 5 mins 7 secs
--

--DELETE FROM fcs_migration.vent_long_term_years;
--DELETE FROM fcs_migration.flare_long_term_years;
--DELETE FROM fcs_migration.application_technical_reviews;
--DELETE FROM fcs_migration.application_updates;
--DELETE FROM fcs_migration.application_case_notes;
--DELETE FROM fcs_migration.vent_report_123_months;
--DELETE FROM fcs_migration.vent_report_123_gas_data;
--DELETE FROM fcs_migration.vent_short_term_123_months;
--DELETE FROM fcs_migration.vent_annual_123_months;
--DELETE FROM fcs_migration.vents;
--DELETE FROM fcs_migration.vent_report_months;
--DELETE FROM fcs_migration.vent_report_periods;
--DELETE FROM fcs_migration.vent_report_gas_data;
--DELETE FROM fcs_migration.vent_short_term_months;
--DELETE FROM fcs_migration.vent_annual_months;
--DELETE FROM fcs_migration.flare_report_123_months;
--DELETE FROM fcs_migration.flare_report_123_gas_data;
--DELETE FROM fcs_migration.flare_short_term_123_months;
--DELETE FROM fcs_migration.flare_annual_123_months;
--DELETE FROM fcs_migration.flares;
--DELETE FROM fcs_migration.flare_report_months;
--DELETE FROM fcs_migration.flare_report_periods;
--DELETE FROM fcs_migration.flare_report_gas_data;
--DELETE FROM fcs_migration.flare_short_term_months;
--DELETE FROM fcs_migration.flare_annual_months;
--DELETE FROM fcs_migration.short_term_production_months;
--DELETE FROM fcs_migration.annual_production_months;
--DELETE FROM fcs_migration.long_term_production_years;
--DELETE FROM fcs_migration.application_supporting_information;
--DELETE FROM fcs_migration.application_eia_directions;
--DELETE FROM fcs_migration.application_flags;
--DELETE FROM fcs_migration.application_units;
--DELETE FROM fcs_migration.application_asset_licences;
--DELETE FROM fcs_migration.application_assets;
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
  -- exclude the below status pairs so that we don't have migrated applications without any corresponding
  -- application versions
  AND (ifcd.status, ifcd.version_status) NOT IN (
    ('INPROGRESS', 'PENDING') -- an unsubmitted application update (for any version/variation) (don't migrate)
  , ('INPROGRESS', 'CURRENT') -- an unsubmitted application (version 1 variation 0) (don't migrate)
  )
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
, cam_wua_id
, migrated
)
WITH stage_assignments AS (
  SELECT /*+ materialize */
    xbc.primary_data_uref fcd_uref
  , wua.id wua_id
  , bs.end_datetime bs_end_datetime
  , CASE c.assignment
    WHEN 'FC_R10_DTI_ADMIN' THEN 'CONSENTS_AND_AUTHORISATIONS_MANAGER'
    ELSE 'CASE_OFFICER'
    END assignment_role
  FROM bpmmgr.xview_business_contexts xbc
  JOIN bpmmgr.business_routine_contexts brc ON xbc.bc_id = brc.bc_id
  JOIN bpmmgr.business_stages bs ON brc.id = bs.brc_id
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
  , RANK () OVER (PARTITION BY sa.fcd_uref, sa.assignment_role ORDER BY sa.bs_end_datetime DESC NULLS FIRST) rank_rownum -- unended stages are at the top
  FROM stage_assignments sa
)
, tip_assignments AS (
  SELECT
    rsa.*
  FROM ranked_stage_assignments rsa
  WHERE rsa.rank_rownum = 1
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
    AND ta.assignment_role = 'CONSENTS_AND_AUTHORISATIONS_MANAGER'
  ) cam_wua_id
, 'true' migrated
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

UPDATE fcs_migration.application_versions
SET current_case_owner =
  CASE
  WHEN cam_wua_id IS NOT NULL THEN 'CONSENTS_AND_AUTHORISATIONS_MANAGER' -- assigned after the case officer
  WHEN case_officer_wua_id IS NOT NULL THEN 'CASE_OFFICER'
  END;
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
) cl;

--
-- application_assets
--

INSERT INTO fcs_migration.application_assets (
  id
, application_version_id
, asset_role
, asset_no
, asset_operator_ou_id
, cached_asset_operator_name
, asset_type
, asset_id
, cached_asset_name
)
-- find how many fields are in an application version
WITH field_counts AS (
  SELECT count(*) field_count, xfcf.fcd_id
  FROM envmgr.xview_field_consent_fields xfcf
  GROUP BY xfcf.fcd_id
)
-- find where the facility location matches one of the fields being consented
, field_locations AS (
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
    xfcd.fcd_id application_version_id
  , CASE
    WHEN c.field_count = 1 THEN 'PRIMARY' -- if only 1 field it must be the primary
    WHEN xfcf.field_id = fl.primary_field_id THEN 'PRIMARY'
    -- where the location field isn't a field in the list just use the first field for the PRIMARY
    WHEN fl.primary_field_id IS NULL AND xfcf.field_rownum = 1 THEN 'PRIMARY'
    ELSE 'SECONDARY'
    END asset_role
  , coalesce(xfcf.field_operator_ou_id, xfcd.operator_ou_id) asset_operator_ou_id -- field_operator_ou_id is null for PRODUCTION apps
  , ou.name cached_asset_operator_name
  , xfcf.field_id asset_id
  , f.name cached_asset_name
  , xfcf.field_rownum
  FROM fcs_migration.application_versions av
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = av.id
  JOIN envmgr.field_consent_details fcd ON fcd.id = xfcd.fcd_id
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT/COVER_INFO/FIELD_LIST/FIELD'
    PASSING
      fcd.xml_data
    COLUMNS
      field_rownum FOR ORDINALITY
    , field_id INTEGER PATH './FIELD_INFO/FIELD_ID/text()'
    , field_operator_ou_id INTEGER PATH 'FIELD_OPERATOR_OU_ID/text()' 
  ) xfcf
  JOIN decmgr.xview_organisation_units ou ON ou.organ_id = coalesce(xfcf.field_operator_ou_id, xfcd.operator_ou_id)
  JOIN devukmgr.fields f ON f.field_identifier = xfcf.field_id
  JOIN field_counts c ON c.fcd_id = xfcd.fcd_id
  LEFT JOIN field_locations fl ON fl.fcd_id = xfcd.fcd_id
  ORDER BY xfcd.fcd_id ASC
)
SELECT
  fcs_migration.application_asset_id_seq.nextval id
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
FROM base b;
/

--
-- LOCATION application_assets
--

INSERT INTO fcs_migration.application_assets (
  id
, application_version_id
, asset_role
, asset_operator_ou_id
, cached_asset_operator_name
, asset_type
, asset_id
, cached_asset_name
)
SELECT
  fcs_migration.application_asset_id_seq.nextval id
, fcd.id application_version_id
, 'LOCATION' asset_role
, fov.operator_id asset_operator_ou_id
, fov.operator_name cached_asset_operator_name
, 'FIELD' asset_type
, ff.facilities_location_field_id asset_id
, f.name cached_asset_name
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
JOIN devukmgr.field_operator_view fov ON f.field_identifier = fov.field_id -- TODO what about the fields that have no operator?
WHERE ff.facilities_location_field_id IS NOT NULL;
/

--
-- application_asset_licences
--

INSERT INTO fcs_migration.application_asset_licences (
  id
, application_version_id
, application_asset_id
, licence_id
, cached_licence_ref
)
WITH licences AS (
  SELECT
    plm.id licence_id
  , (pcdp.licence_type || pcdp.licence_no) licence_number
  FROM pedmgr.ped_licence_master plm
  JOIN pedmgr.ped_current_data_points pcdp ON pcdp.licence_no = plm.licence_no AND pcdp.licence_type = plm.licence_type
  WHERE plm.system_status = 'LIVE'
  AND pcdp.ped_sim_id = 0
)
SELECT
  fcs_migration.application_asset_licence_id_seq.nextval id
, aa.application_version_id
, aa.id application_asset_id
, li.licence_id
, li.licence_number cached_licence_ref
FROM fcs_migration.application_assets aa
JOIN envmgr.xview_field_consent_licences xfcl ON xfcl.fcd_id = aa.application_version_id AND xfcl.field_id = aa.asset_id
JOIN licences li ON li.licence_number = xfcl.licence_number
WHERE aa.asset_role IN ('PRIMARY', 'SECONDARY')
AND aa.asset_type = 'FIELD';
/

--
-- application_units
--

INSERT INTO fcs_migration.application_units (
  id
, application_version_id
, production_oil_unit
, production_gas_unit
)
SELECT
  fcs_migration.application_unit_id_seq.nextval id
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
WHERE a.type = 'PRODUCTION';
/
INSERT INTO fcs_migration.application_units (
  id
, application_version_id
, flare_category_unit
, flare_gas_density_unit
, flare_gas_content_unit
, emission_category_type
)
SELECT
  fcs_migration.application_unit_id_seq.nextval id
, av.id application_version_id
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'TONNES_PER_DAY'
  WHEN 'ANNUAL' THEN 'TONNES_PER_MONTH'
  WHEN 'SHORT_TERM' THEN 'TONNES_PER_MONTH'
  END flare_category_unit
, 'G_PER_MOL' flare_gas_density_unit
, 'MOL_PERCENTAGE' flare_gas_content_unit
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'LEGACY_LONG_TERM'
  ELSE
    CASE cat.categories
    WHEN '1_2_3' THEN 'CATEGORY_123'
    WHEN 'A_B_C' THEN 'CATEGORY_ABC'
    END
  END emission_category_type
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    categories VARCHAR2(4000) PATH './FLAGS/CATEGORIES/text()'
) cat
WHERE a.type = 'FLARE';
/
INSERT INTO fcs_migration.application_units (
  id
, application_version_id
, vent_category_unit
, vent_gas_density_unit
, vent_gas_content_unit
, emission_category_type
)
SELECT
  fcs_migration.application_unit_id_seq.nextval id
, av.id application_version_id
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'TONNES_PER_DAY'
  WHEN 'ANNUAL' THEN 'TONNES_PER_MONTH'
  WHEN 'SHORT_TERM' THEN 'TONNES_PER_MONTH'
  END vent_category_unit
, 'G_PER_MOL' vent_gas_density_unit
, 'MOL_PERCENTAGE' vent_gas_content_unit
, CASE cl.consent_length
  WHEN 'LONG_TERM' THEN 'LEGACY_LONG_TERM'
  ELSE
    CASE cat.categories
    WHEN '1_2_3' THEN 'CATEGORY_123'
    WHEN 'A_B_C' THEN 'CATEGORY_ABC'
    END
  END emission_category_type
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    categories VARCHAR2(4000) PATH './FLAGS/CATEGORIES/text()'
) cat
WHERE a.type = 'VENT';
/

--
-- application_flags
--

-- WILL_GAS_BE_INJECTED
INSERT INTO fcs_migration.application_flags (
  id
, application_version_id
, flag_type
, flag_value
)
SELECT
  fcs_migration.application_flag_id_seq.nextval id
, fcd.id application_version_id
, 'WILL_GAS_BE_INJECTED' flag_type
, gas.injection_question flag_value
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

-- HAS_SECONDARY_ASSETS
INSERT INTO fcs_migration.application_flags (
  id
, application_version_id
, flag_type
, flag_value
)
WITH hsa AS (
  SELECT
    aa.application_version_id
  , 'HAS_SECONDARY_ASSETS' flag_type
  , CASE
    WHEN count(*) > 1 THEN 'true'
    ELSE 'false'
    END flag_value
  FROM fcs_migration.applications a
  JOIN fcs_migration.application_versions av ON av.application_id = a.id
  JOIN fcs_migration.application_assets aa ON aa.application_version_id = av.id
  WHERE a.type IN ('FLARE', 'VENT')
  AND (aa.asset_role IS NULL OR aa.asset_role IN ('PRIMARY', 'SECONDARY')) -- NULL means an unclassified PRIMARY or SECONDARY asset at the moment
  GROUP BY aa.application_version_id
)
SELECT
  fcs_migration.application_flag_id_seq.nextval id
, hsa.application_version_id
, hsa.flag_type
, hsa.flag_value
FROM hsa;
/

-- IS_ACE_APPLICATION
INSERT INTO fcs_migration.application_flags (
  id
, application_version_id
, flag_type
, flag_value
)
SELECT
  fcs_migration.application_flag_id_seq.nextval id
, av.id application_version_id
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
FROM fcs_migration.applications a
JOIN fcs_migration.application_versions av ON av.application_id = a.id
JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
WHERE av.submitted_date_time IS NOT NULL;

--
-- application_eia_directions
--
INSERT INTO fcs_migration.application_eia_directions (
  id
, application_version_id
, for_purpose_of_eia_regs
)
SELECT
  fcs_migration.application_eia_direction_id_seq.nextval id
, fcd.id application_version_id
, eia.project_under_eia_regs
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    project_under_eia_regs VARCHAR(5) PATH 'ADDITIONAL_INFO/PROJECT_UNDER_EIA_REGS/text()'
) eia
WHERE eia.project_under_eia_regs IS NOT NULL
AND fcd.application_type = 'PCON'; -- TODO - what about the FCON and VCON data?
/

--
-- application_supporting_information
--
INSERT INTO fcs_migration.application_supporting_information (
  id
, application_version_id
, notes
, erap_notes
)
SELECT
  fcs_migration.application_supporting_information_id_seq.nextval id
, fcd.id
, hi.note_text
, hi.imp_note_text
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_INFO/HISTORY_LIST'
  PASSING
    fcd.xml_data
  COLUMNS
    -- the items with EDITABLE_FLAG true are the latest version of that note TYPE
    note_text CLOB PATH './HISTORY_ITEM[TYPE/text()="ADDITIONAL_NOTE"][EDITABLE_FLAG/text()="true"]/CONTENT/text()'
  , imp_note_text CLOB PATH './HISTORY_ITEM[TYPE/text()="IMPROVEMENT_NOTE"][EDITABLE_FLAG/text()="true"]/CONTENT/text()'
) hi;
/

--
-- long_term_production_years
--
INSERT INTO fcs_migration.long_term_production_years (
  id
, application_version_id
, year
, oil_min_value
, oil_max_value
, gas_min_value
, gas_max_value
)
SELECT
  fcs_migration.long_term_production_year_id_seq.nextval id
, fcd.id application_version_id
, ltp.year
, coalesce(ltp.oil_min_value, 0) oil_min_value
, ltp.oil_max_value
, coalesce(ltp.gas_min_value, 0) gas_min_value
, ltp.gas_max_value
FROM fcs_migration.application_versions av
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
AND xfcd.app_length = 'LONG_TERM';
/

--
-- annual_production_months
--

INSERT INTO fcs_migration.annual_production_months (
  id
, application_version_id
, year
, month
, oil_min_value
, oil_max_value
, gas_min_value
, gas_max_value
)
SELECT
  fcs_migration.annual_production_month_id_seq.nextval id
, fcd.id application_version_id
, xfcd.application_year year
, upper(trim(ap.description)) month
, coalesce(ap.oil_min_value, 0) oil_min_value
, ap.oil_max_value
, coalesce(ap.gas_min_value, 0) gas_min_value
, ap.gas_max_value
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ANNUAL_PRODUCTION/PRODUCTION_DATA_LIST/PRODUCTION_DATA[./TYPE/text()="MONTH"]'
  PASSING
    fcd.xml_data
  COLUMNS
    description VARCHAR2(4000) PATH './DESCRIPTION/text()'
  , oil_min_value NUMBER PATH './OIL_MIN/text()'
  , oil_max_value NUMBER PATH './OIL/text()'
  , gas_min_value NUMBER PATH './GAS_MIN/text()'
  , gas_max_value NUMBER PATH './GAS/text()'
) ap
WHERE fcd.application_type = 'PCON'
AND xfcd.app_length = 'ANNUAL'
AND coalesce(ap.oil_min_value, ap.oil_max_value, ap.gas_max_value, ap.gas_max_value) IS NOT NULL;
/

--
-- short_term_production_months
--
INSERT INTO fcs_migration.short_term_production_months (
  id
, application_version_id
, year
, month
, start_date
, end_date
, oil_min_value
, oil_max_value
, gas_min_value
, gas_max_value
)
WITH base AS (
  SELECT
    fcd.id application_version_id
  , stp.pd_rownum
  , stp.data_year
  , upper(trim(stp.description)) month
  , coalesce(stp.oil_min_value, 0) oil_min_value
  , stp.oil_max_value
  , coalesce(stp.gas_min_value, 0) gas_min_value
  , stp.gas_max_value
  , cl.short_term_start_date
  , cl.short_term_end_date
  FROM fcs_migration.application_versions av
  JOIN fcs_migration.consent_lengths cl ON cl.application_version_id = av.id
  JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT/SHORT_TERM_CONSENT/CONSENT_DATA_LIST/CONSENT_DATA[./TYPE/text()="MONTH"]'
    PASSING
      fcd.xml_data
    COLUMNS
      pd_rownum FOR ORDINALITY
    , data_year INTEGER PATH './DATA_YEAR/text()'
    , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
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
  SELECT
    b.*
  , to_date('01'||b.month||b.data_year, 'DDMONTHYYYY') month_start_date
  , last_day(to_date('01'||b.month||b.data_year, 'DDMONTHYYYY')) month_end_date
  FROM base b
)
SELECT
  fcs_migration.short_term_production_month_id_seq.nextval id
, b.application_version_id
, b.data_year year
, b.month
, greatest(b.short_term_start_date, b.month_start_date) start_date
, least(b.short_term_end_date, b.month_end_date) end_date
, b.oil_min_value
, b.oil_max_value
, b.gas_min_value
, b.gas_max_value
FROM base2 b;
/

--
-- flare_annual_months
--

INSERT INTO fcs_migration.flare_annual_months (
  id
, application_version_id
, year
, month
, category_a
, category_b
, category_c
, comments
)
SELECT
  fcs_migration.flare_annual_month_id_seq.nextval id
, fcd.id application_version_id
, ed.year
, ed.month
, ed.category_a
, ed.category_b
, ed.category_c
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
AND ed.categories = 'A_B_C';
/

--
-- flare_annual_123_months
--

INSERT INTO fcs_migration.flare_annual_123_months (
  id
, application_version_id
, year
, month
, category_1
, category_2
, category_3
, comments
)
SELECT
  fcs_migration.flare_annual_123_month_id_seq.nextval id
, fcd.id application_version_id
, ed.year
, ed.month
, ed.category_1
, ed.category_2
, ed.category_3
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON'
AND ed.categories = '1_2_3';
/

--
-- flare_short_term_months
--

INSERT INTO fcs_migration.flare_short_term_months (
  id
, application_version_id
, year
, month
, start_date
, end_date
, category_a
, category_b
, category_c
, comments
)
SELECT
  fcs_migration.flare_short_term_month_id_seq.nextval id
, ed.fcd_id application_version_id
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
AND ed.categories = 'A_B_C';
/

--
-- flare_short_term_123_months
--

INSERT INTO fcs_migration.flare_short_term_123_months (
  id
, application_version_id
, year
, month
, start_date
, end_date
, category_1
, category_2
, category_3
, comments
)
SELECT
  fcs_migration.flare_short_term_123_month_id_seq.nextval id
, ed.fcd_id application_version_id
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
AND ed.categories = '1_2_3';
/

--
-- flare_long_term_years
--
INSERT INTO fcs_migration.flare_long_term_years (
  id
, application_version_id
, year
, gas
)
SELECT
  fcs_migration.flare_long_term_year_id_seq.nextval id
, fcd.id application_version_id
, ed.year
, ed.gas
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_long_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON';
/

--
-- flare_report_gas_data
--
INSERT INTO fcs_migration.flare_report_gas_data (
  id
, application_version_id
, category_a_density
, category_a_inert_percentage
, category_a_hydro_percentage
, category_b_density
, category_b_inert_percentage
, category_b_hydro_percentage
, category_c_density
, category_c_inert_percentage
, category_c_hydro_percentage
, evaluated_per_category
, evaluated_per_category_explanation
)
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
  AND fcd.application_type = 'FCON'
  AND rd.categories = 'A_B_C'
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
  AND coalesce(rd.category_a, rd.category_b, rd.category_c) IS NOT NULL
  ORDER BY rd.fcd_id, rd.rd_rownum
)
-- below required as we may be missing a row for any of the 3 data type
-- so use this as the starting point for the pivot query below then use
-- LEFT joins for all there data_type sets
, base_av AS (
  SELECT b.application_version_id
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  fcs_migration.flare_report_gas_data_id_seq.nextval id
, b.application_version_id
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
-- flare_report_123_gas_data
--
INSERT INTO fcs_migration.flare_report_123_gas_data (
  id
, application_version_id
, category_1_density
, category_1_inert_percentage
, category_1_hydro_percentage
, category_2_density
, category_2_inert_percentage
, category_2_hydro_percentage
, category_3_density
, category_3_inert_percentage
, category_3_hydro_percentage
)
WITH base AS (
  SELECT
    fcd.id application_version_id
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
-- below required as we may be missing a row for any of the 3 data type
-- so use this as the starting point for the pivot query below then use
-- LEFT joins for all there data_type sets
, base_av AS (
  SELECT b.application_version_id
  FROM base b
  GROUP BY b.application_version_id
)
SELECT
  fcs_migration.flare_report_123_gas_data_id_seq.nextval id
, b.application_version_id
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
/

--
-- flare_report_periods
--
INSERT INTO fcs_migration.flare_report_periods (
  id
, application_version_id
, report_end_month
, report_end_year
)
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
  AND fcd.application_type = 'FCON'
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
  fcs_migration.flare_report_period_id_seq.nextval id
, re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;
/
--
-- 1_2_3 report periods go into the same table as A_B_C
--
INSERT INTO fcs_migration.flare_report_periods (
  id
, application_version_id
, report_end_month
, report_end_year
)
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
  fcs_migration.flare_report_period_id_seq.nextval id
, re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;
/

--
-- flare_report_months
--
INSERT INTO fcs_migration.flare_report_months (
  id
, application_version_id
, year
, month
, category_a
, category_b
, category_c
, shut_down_days
, comments
)
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
  AND fcd.application_type = 'FCON'
  AND rd.categories = 'A_B_C'
  AND xfcd.app_length IN ('ANNUAL', 'SHORT_TERM')
  AND coalesce(rd.category_a, rd.category_b, rd.category_c, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
SELECT
  fcs_migration.flare_report_month_id_seq.nextval id
, b.application_version_id
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
INSERT INTO fcs_migration.flare_report_123_months (
  id
, application_version_id
, year
, month
, category_1
, category_2
, category_3
, shut_down_days
, comments
)
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
  AND fcd.application_type = 'FCON'
  AND rd.categories = '1_2_3'
  AND xfcd.app_length = 'ANNUAL'
  AND coalesce(rd.category_1, rd.category_2, rd.category_3, rd.days_total_shutdown) IS NOT NULL
  ORDER BY fcd.id, rd.rd_rownum
)
SELECT
  fcs_migration.flare_report_123_month_id_seq.nextval id
, b.application_version_id
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
INSERT INTO fcs_migration.flares (
  id
, application_version_id
, flare_no
, flare_type
, description
, metered_flag
, comments
)
SELECT
  fcs_migration.flare_id_seq.nextval id
, fcd.id application_version_id
, RANK () OVER (PARTITION BY fcd.id ORDER BY es.es_rownum) flare_no
, es.type flare_type
, es.description
, es.metered metered_flag
, es.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_emission_systems es ON es.fcd_id = fcd.id
WHERE fcd.application_type = 'FCON';
/

--
-- vent_annual_months
--
INSERT INTO fcs_migration.vent_annual_months (
  id
, application_version_id
, year
, month
, category_a
, category_b
, category_c
, comments
)
SELECT
  fcs_migration.vent_annual_month_id_seq.nextval id
, fcd.id application_version_id
, ed.year
, ed.month
, ed.category_a
, ed.category_b
, ed.category_c
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = 'A_B_C';
/

--
-- vent_annual_123_months
--
INSERT INTO fcs_migration.vent_annual_123_months (
  id
, application_version_id
, year
, month
, category_1
, comments
)
SELECT
  fcs_migration.vent_annual_123_month_id_seq.nextval id
, fcd.id application_version_id
, ed.year
, ed.month
, ed.category_1
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_annual_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = '1_2_3'
-- category_1 data is null for the legacy terminal apps (i.e. there's no consent months data so don't migrate)
AND ed.category_1 IS NOT NULL;
/

--
-- vent_short_term_months
--

INSERT INTO fcs_migration.vent_short_term_months (
  id
, application_version_id
, year
, month
, start_date
, end_date
, category_a
, category_b
, category_c
, comments
)
SELECT
  fcs_migration.vent_short_term_month_id_seq.nextval id
, ed.fcd_id application_version_id
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
AND ed.categories = 'A_B_C';
/

--
-- vent_short_term_123_months
--
INSERT INTO fcs_migration.vent_short_term_123_months (
  id
, application_version_id
, year
, month
, start_date
, end_date
, category_1
, comments
)
SELECT
  fcs_migration.vent_short_term_123_month_id_seq.nextval id
, ed.fcd_id application_version_id
, ed.year
, ed.month
, ed.start_date
, ed.end_date
, ed.category_1
, ed.comments
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_short_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON'
AND ed.categories = '1_2_3'
-- category_1 data is null for the legacy terminal apps (i.e. there's no consent months data so don't migrate)
AND ed.category_1 IS NOT NULL;
/

--
-- vent_long_term_years
--
INSERT INTO fcs_migration.vent_long_term_years (
  id
, application_version_id
, year
, gas
)
SELECT
  fcs_migration.vent_long_term_year_id_seq.nextval id
, fcd.id application_version_id
, ed.year
, ed.gas
FROM fcs_migration.application_versions av
JOIN envmgr.field_consent_details fcd ON fcd.id = av.id
JOIN fcs_migration.field_consent_long_term_emission_data ed ON ed.fcd_id = fcd.id
WHERE fcd.application_type = 'VCON';
/

--
-- vent_report_gas_data
--
INSERT INTO fcs_migration.vent_report_gas_data (
  id
, application_version_id
, category_a_density
, category_a_inert_percentage
, category_a_hydro_percentage
, category_b_density
, category_b_inert_percentage
, category_b_hydro_percentage
, category_c_density
, category_c_inert_percentage
, category_c_hydro_percentage
, evaluated_per_category
, evaluated_per_category_explanation
)
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
  fcs_migration.vent_report_gas_data_id_seq.nextval id
, b.application_version_id
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
LEFT JOIN base bh ON bh.application_version_id = b.application_version_id AND bh.data_type = 'HYDRO';
/

--
-- vent_report_123_gas_data
--
INSERT INTO fcs_migration.vent_report_123_gas_data (
  id
, application_version_id
, category_1_density
, category_1_inert_percentage
, category_1_hydro_percentage
)
WITH base AS (
  SELECT
    fcd.id application_version_id
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
  fcs_migration.vent_report_123_gas_data_id_seq.nextval id
, b.application_version_id
, bd.category_1 category_1_density
, bi.category_1 category_1_inert_percentage
, bh.category_1 category_1_hydro_percentage
FROM base_av b
LEFT JOIN base bd ON bd.application_version_id = b.application_version_id AND bd.data_type = 'DENSITY'
LEFT JOIN base bi ON bi.application_version_id = b.application_version_id AND bi.data_type = 'INERT'
LEFT JOIN base bh ON bh.application_version_id = b.application_version_id AND bh.data_type = 'HYDRO';
/

--
-- vent_report_periods
--
INSERT INTO fcs_migration.vent_report_periods (
  id
, application_version_id
, report_end_month
, report_end_year
)
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
  fcs_migration.vent_report_period_id_seq.nextval id
, re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;
/
--
-- 1_2_3 report periods go into the same table as A_B_C
--
INSERT INTO fcs_migration.vent_report_periods (
  id
, application_version_id
, report_end_month
, report_end_year
)
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
  fcs_migration.vent_report_period_id_seq.nextval id
, re.application_version_id
, to_char(re.report_row_end_date, 'MONTH') report_end_month
, to_number(to_char(re.report_row_end_date, 'YYYY')) report_end_year
FROM report_end re;
/


--
-- vent_report_months
--
INSERT INTO fcs_migration.vent_report_months (
  id
, application_version_id
, year
, month
, category_a
, category_b
, category_c
, shut_down_days
, comments
)
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
  fcs_migration.vent_report_month_id_seq.nextval id
, b.application_version_id
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
INSERT INTO fcs_migration.vent_report_123_months (
  id
, application_version_id
, year
, month
, category_1
, shut_down_days
, comments
)
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
  fcs_migration.vent_report_123_month_id_seq.nextval id
, b.application_version_id
, to_number(to_char(b.report_row_end_date, 'YYYY')) year
, to_char(b.report_row_end_date, 'MONTH') month
, coalesce(b.category_1, 0) category_1 -- some are null with a non null days_total_shutdown
, b.days_total_shutdown
, b.comments
FROM base b;
/

--
-- vents
--
INSERT INTO fcs_migration.vents (
  id
, application_version_id
, vent_no
, vent_type
, description
, metered_flag
, comments
)
SELECT
  fcs_migration.vent_id_seq.nextval id
, fcd.id application_version_id
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
INSERT INTO fcs_migration.application_case_notes (
  id
, application_version_id
, added_by_wua_id
, added_date_time
, case_note_text
)
SELECT
  fcs_migration.application_case_note_id_seq.nextval id
, fci.fcd_id application_version_id
, fci.created_by_wua_id added_by_wua_id
, fci.created_datetime added_date_time
, fci.intention_html case_note_text
FROM fcs_migration.application_versions av
JOIN fcs_migration.field_consent_intentions fci ON fci.fcd_id = av.id
WHERE fci.class_type = 'FC_GENERAL_NOTE';
/

--
-- application_updates
--
INSERT INTO fcs_migration.application_updates (
  id
, application_version_id
, requested_by_wua_id
, requested_date_time
, request_text
, deadline_date_time
, responded_by_wua_id
, responded_date_time
, response_text
, response_type
, application_update_status
, response_application_version_id
)
WITH resp AS (
  SELECT
    av.id application_version_id
  , avnext.id response_application_version_id
  , avnext.submitted_by_wua_id responded_by_wua_id
  , avnext.submitted_date_time responded_date_time
  FROM fcs_migration.application_versions av
  JOIN fcs_migration.application_versions avnext ON avnext.id = (
    SELECT min(av2.id) -- if a variation is withdrawn they could start a variation again (gets same number) and therefore have more that one next version within the variation's set of versions (see fc_id = 1384 on dev)
    FROM fcs_migration.application_versions av2
    WHERE av2.application_id = av.application_id
    AND av2.version_no = av.version_no + 1
    AND av2.status != 'DELETED'
    AND av2.created_date_time > av.created_date_time
  )
)
, base AS (
  SELECT
    av.id application_version_id
  , fci.created_by_wua_id requested_by_wua_id
  , fci.created_datetime requested_date_time
  , fci.intention_html request_text
  -- you have to do the subqueries here as you can't LEFT OUTER JOIN on and INSERT INTO
  , (SELECT r.responded_by_wua_id FROM resp r WHERE r.application_version_id = av.id) responded_by_wua_id
  , (SELECT r.responded_date_time FROM resp r WHERE r.application_version_id = av.id) responded_date_time
  , (SELECT r.response_application_version_id FROM resp r WHERE r.application_version_id = av.id) response_application_version_id
  FROM fcs_migration.application_versions av
  JOIN fcs_migration.field_consent_intentions fci ON fci.fcd_id = av.id
  WHERE (fci.class_type, fci.severity) IN (
    ('FC_NOTE_FOR_OPERATOR', 'NONE')
  , ('FC_REVIEW_DECISION', 'REQUEST_UPDATE')
  )
  ORDER BY fci.created_datetime
)
SELECT
  fcs_migration.application_update_id_seq.nextval id
, b.application_version_id
, b.requested_by_wua_id
, b.requested_date_time
, b.request_text
, NULL deadline_date_time
, b.responded_by_wua_id
, b.responded_date_time
, NULL response_text
, NULL response_type
, 'CLOSED' application_update_status -- best to mark all closed I think
, b.response_application_version_id
FROM base b;
/

--
-- application_technical_reviews
--
INSERT INTO fcs_migration.application_technical_reviews (
  id
, request_application_version_id
, requested_by_wua_id
, requested_date_time
, request_text
, deadline_date_time
, technical_reviewer_wua_id
, responded_by_wua_id
, responded_date_time
, response_text
, response_type
, technical_review_status
, response_application_version_id
)
WITH isetins AS (
  SELECT isi.is_id
  , '<p>'||xtcd.title||':</p>'||XMLQUERY('/CLAUSE_TEXT/node()' PASSING xid.clause_text RETURNING CONTENT).getClobVal() response_text
  FROM bpmmgr.review_advisor_slot_details rasd
  JOIN bpmmgr.xview_intention_sets xis ON xis.is_id = rasd.intention_set_id
  JOIN bpmmgr.intention_set_intentions isi ON isi.is_id = xis.is_id AND isi.end_datetime IS NULL
  JOIN bpmmgr.intentions i ON i.id = isi.in_id
  JOIN bpmmgr.xview_intention_details xid ON xid.in_id = i.id AND xid.end_datetime IS NULL
  JOIN bpmmgr.xview_template_clause_details xtcd ON xtcd.clause_type_id = xid.clause_type AND xtcd.class = xid.class_type
  WHERE upper(rasd.name) LIKE '%FIELD CONSENT REVIEW%'
  AND xis.domain = 'RESPONSE'
  AND xis.primary_data_uref = 'PSUEDO_SLOT_MSD'
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
  WHERE m.aac_mem_rank = 1 -- we grab the last member added to the aac team ELECTRONIC_ADVISOR_AUTO role (only 1 member of this role type exists for each aac in legacy field consents)
)
SELECT
  fcs_migration.application_technical_review_id_seq.nextval id
, (
  SELECT av2.id -- the latest app version at the time of the review
  FROM fcs_migration.application_versions av2
  WHERE av2.application_id = av.application_id
  AND xrad.review_delivered_date > av2.created_date_time
  AND av2.status != 'DELETED'
  ORDER BY av2.created_date_time DESC
  FETCH FIRST 1 ROWS ONLY
  ) request_application_version_id
, rreqd.created_by_wua_id requested_by_wua_id
, xrad.review_delivered_date requested_date_time
, NULL request_text
, xrad.review_deadline_date deadline_date_time
, coalesce(aac_wuas.wua_id, rasd.status_by_wua_id) technical_reviewer_wua_id
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
FROM fcs_migration.application_versions av
JOIN bpmmgr.xview_review_inv_details rid ON rid.primary_data_uref = av.id||'FC'
JOIN bpmmgr.review_runs rr ON rr.ri_id = rid.ri_id
JOIN bpmmgr.review_requests rreq ON rreq.rrun_id = rr.id
JOIN bpmmgr.review_request_details rreqd ON rreqd.rreq_id = rreq.id AND rreqd.status_control = 'C'
JOIN bpmmgr.review_advisors ra ON ra.rreq_id = rreq.id
JOIN bpmmgr.review_advisor_details rad ON ra.id = rad.ra_id AND rad.status_control = 'C'
JOIN bpmmgr.xview_review_advisor_details xrad ON xrad.ra_id = rad.ra_id AND xrad.status_control = 'C'
JOIN bpmmgr.review_advisor_slots ras ON ras.ra_id = ra.id
JOIN bpmmgr.review_advisor_slot_details rasd ON ras.id = rasd.ras_id AND rasd.status_control = 'C'
LEFT JOIN isets ON isets.is_id = rasd.intention_set_id
LEFT JOIN aac_wuas ON aac_wuas.aac_id = rreq.aac_id
WHERE rid.status_control = 'C';
/
COMMIT;
/
