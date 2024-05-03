CREATE OR REPLACE VIEW fcs_migration.field_consent_application_consent_data AS
SELECT
  fci.fcd_id
, fci.application_type
, cd.app_length
, cd.valid_from_date
, cd.schedule_valid_from_date
, cd.valid_to_date
, st.to_number_safe(cd.production_oil_min_average) production_oil_min_average
, st.to_number_safe(cd.production_oil_max_average) production_oil_max_average
, st.to_number_safe(cd.production_gas_min_average) production_gas_min_average
, st.to_number_safe(cd.production_gas_max_average) production_gas_max_average
, st.to_number_safe(cd.flare_average) flare_average
, st.to_number_safe(cd.vent_average) vent_average
FROM envmgr.field_consents_issued fci
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_DOC_DATA'
  PASSING
    fci.xml_data
  COLUMNS
    app_length VARCHAR2(4000) PATH './APP_LENGTH/text()'
  , valid_from_date DATE PATH './VALID_FROM_DATE/text()'
  , schedule_valid_from_date DATE PATH './SCHEDULE_VALID_FROM_DATE/text()'
  , valid_to_date DATE PATH './VALID_TO_DATE/text()'
  , production_oil_min_average VARCHAR2(4000) PATH './ANNUAL_OIL_MIN_AVERAGE/text()'
  , production_oil_max_average VARCHAR2(4000) PATH './ANNUAL_OIL_AVERAGE/text()'
  , production_gas_min_average VARCHAR2(4000) PATH './ANNUAL_GAS_MIN_AVERAGE/text()'
  , production_gas_max_average VARCHAR2(4000) PATH './ANNUAL_GAS_AVERAGE/text()'
  , flare_average VARCHAR2(4000) PATH './FLARE_AVERAGE/text()'
  , vent_average VARCHAR2(4000) PATH './VENT_AVERAGE/text()'
) cd;
