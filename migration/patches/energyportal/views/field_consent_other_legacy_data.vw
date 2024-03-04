CREATE OR REPLACE VIEW fcs_migration.field_consent_other_legacy_data AS
SELECT
  fcd.id fcd_id
, CASE fcd.application_type
  WHEN 'PCON' THEN ld.increase_in_production
  END increase_in_production
, CASE fcd.application_type
  WHEN 'PCON' THEN ld.es_reference
  END es_reference
, CASE fcd.application_type
  WHEN 'PCON' THEN
    CASE xfcd.app_length
    WHEN 'ANNUAL' THEN st.to_number_safe(ld.annual_uplift)
    WHEN 'LONG_TERM' THEN st.to_number_safe(ld.long_term_uplift)
    END
  END uplift_percentage
, CASE
  WHEN fcd.application_type = 'VCON' AND xfcd.app_length IN ('SHORT_TERM', 'ANNUAL') THEN
    CASE ld.field_location
    WHEN 'CNS' THEN 'Central North Sea'
    WHEN 'NNSWS' THEN 'Northern North Sea/West of Shetlands'
    WHEN 'IS' THEN 'Irish Sea'
    WHEN 'SNS' THEN 'Southern North Sea'
    WHEN 'ON' THEN 'Onshore'
    END
  END field_location
, CASE
  WHEN fcd.application_type = 'FCON' THEN
    st.to_number_safe(ld.flare_consent_history)
  -- the vent_app_type_default check ensures we only have visible (not stale) dom data
  WHEN fcd.application_type = 'VCON' AND ld.vent_app_type_default = 'true' THEN
    st.to_number_safe(ld.vent_consent_history)
  END previous_year_consent_history
, CASE
  WHEN fcd.application_type = 'FCON' AND xfcd.app_length = 'LONG_TERM' THEN
    st.to_number_safe(ld.flare_actuals)
  -- the vent_app_type_default check ensures we only have visible (not stale) dom data
  WHEN fcd.application_type = 'VCON' AND xfcd.app_length = 'LONG_TERM' AND ld.vent_app_type_default = 'true' THEN
    st.to_number_safe(ld.vent_actuals)
  END previous_year_actuals
, CASE
  -- the vent_app_type_default check ensures we only have visible (not stale) dom data
  WHEN fcd.application_type = 'VCON' AND ld.vent_app_type_default = 'false' THEN
    ld.terminal_name
  END terminal_name
, CASE
  -- the vent_app_type_default check ensures we only have visible (not stale) dom data
  WHEN fcd.application_type = 'VCON' AND ld.vent_app_type_default = 'false' THEN
    ld.terminal_location
  END terminal_location
, CASE
  WHEN fcd.application_type IN ('FCON', 'VCON') THEN -- we are already migrating this for PCON cases
    ld.project_under_eia_regs
  END project_under_eia_regs
FROM envmgr.field_consent_details fcd
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
  -- production only
    increase_in_production VARCHAR2(5) PATH './COVER_INFO/PROD_INCREASE_REF_Q/text()'
  , es_reference VARCHAR2(4000) PATH './COVER_INFO/PROD_INCREASE_REF/text()'
  , annual_uplift VARCHAR2(4000) PATH './ANNUAL_PRODUCTION/UPLIFT/text()'
  , long_term_uplift VARCHAR2(4000) PATH './LONG_TERM_PRODUCTION/UPLIFT/text()'
  -- flare only
  , flare_consent_history VARCHAR2(4000) PATH './COVER_INFO/FLARE_CONSENT_HISTORY/text()'
  , flare_actuals VARCHAR2(4000) PATH './COVER_INFO/FLARE_ACTUALS/text()' -- only needed for long term (for annual and short term this is from the report data) 
  -- vent only
  , vent_app_type_default VARCHAR2(4000) PATH './COVER_INFO/APP_TYPE_DEFAULT/text()'
  , field_location VARCHAR2(4000) PATH './COVER_INFO/FIELD_LOCATION/text()'
  , terminal_name VARCHAR2(4000) PATH './COVER_INFO/TERMINAL_NAME/text()'
  , terminal_location VARCHAR2(4000) PATH './COVER_INFO/TERMINAL_LOCATION/text()'
  , vent_consent_history VARCHAR2(4000) PATH './COVER_INFO/VENT_CONSENT_HISTORY/text()'
  , vent_actuals VARCHAR2(4000) PATH './COVER_INFO/VENT_ACTUALS/text()' -- only needed for long term (for annual and short term this is from the report data)
  -- flare and vent
  , project_under_eia_regs VARCHAR2(5) PATH './ADDITIONAL_INFO/PROJECT_UNDER_EIA_REGS/text()'
) ld;
/