CREATE OR REPLACE VIEW fcs_migration.field_consent_consent_data_long_term_emission_figures AS
SELECT
  fci.fcd_id
, fci.application_type
, cd.app_length
, st.to_number_safe(lt.year) year
, coalesce(st.to_number_safe(lt.gas), 0) daily_average
FROM envmgr.field_consents_issued fci
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_DOC_DATA'
  PASSING
    fci.xml_data
  COLUMNS
    app_length VARCHAR2(4000) PATH './APP_LENGTH/text()'
) cd
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/ADDITIONAL_DOC_DATA/LONG_TERM_CONSENT/DATA_LIST/*'
  PASSING
    fci.xml_data
  COLUMNS
    year VARCHAR2(4000) PATH './YEAR/text()'
  , gas VARCHAR2(4000) PATH './GAS/text()'
) lt
WHERE fci.application_type IN ('FCON', 'VCON')
AND cd.app_length = 'LONG_TERM';
