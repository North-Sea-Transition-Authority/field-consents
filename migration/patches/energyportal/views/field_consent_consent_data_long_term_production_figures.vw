CREATE OR REPLACE VIEW fcs_migration.field_consent_consent_data_long_term_production_figures AS
SELECT
  fci.fcd_id
, fci.application_type
, cd.app_length
, st.to_number_safe(lt.year) year
, coalesce(st.to_number_safe(lt.min_oil), 0) min_oil
, coalesce(st.to_number_safe(lt.max_oil), 0) max_oil
, coalesce(st.to_number_safe(lt.min_gas), 0) min_gas
, coalesce(st.to_number_safe(lt.max_gas), 0) max_gas
FROM envmgr.field_consents_issued fci
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_DOC_DATA'
  PASSING
    fci.xml_data
  COLUMNS
    app_length VARCHAR2(4000) PATH './APP_LENGTH/text()'
) cd
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_DOC_DATA/LONG_TERM_PRODUCTION/PRODUCTION_DATA_LIST/*'
  PASSING
    fci.xml_data
  COLUMNS
    year VARCHAR2(4000) PATH './YEAR/text()'
  , min_oil VARCHAR2(4000) PATH './OIL_MIN/text()'
  , max_oil VARCHAR2(4000) PATH './OIL/text()'
  , min_gas VARCHAR2(4000) PATH './GAS_MIN/text()'
  , max_gas VARCHAR2(4000) PATH './GAS/text()'
) lt
WHERE fci.application_type = 'PCON'
AND cd.app_length = 'LONG_TERM';
