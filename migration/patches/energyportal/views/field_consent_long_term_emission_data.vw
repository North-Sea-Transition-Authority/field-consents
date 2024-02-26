CREATE OR REPLACE VIEW fcs_migration.field_consent_long_term_emission_data AS
SELECT
  fcd.id fcd_id
, ed.ed_rownum
, ed.year
, coalesce(ed.gas, 0) gas
FROM envmgr.field_consent_details fcd
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/LONG_TERM_CONSENT/DATA_LIST/DATA'
  PASSING
    fcd.xml_data
  COLUMNS
    ed_rownum FOR ORDINALITY
  , year INTEGER PATH './YEAR/text()'
  , gas NUMBER PATH './GAS/text()'
) ed
WHERE xfcd.app_length = 'LONG_TERM';
/