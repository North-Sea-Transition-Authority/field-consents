CREATE OR REPLACE VIEW fcs_migration.field_consent_emission_systems AS
SELECT
  fcd.id fcd_id
, es.es_rownum
, CASE upper(es.es_type)
  WHEN 'HP FLARE' THEN 'HP_FLARE'
  WHEN 'MP FLARE' THEN 'MP_FLARE'
  WHEN 'LP FLARE' THEN 'LP_FLARE'
  WHEN 'LLP FLARE' THEN 'LLP_FLARE'
  WHEN 'VENT' THEN 'OTHER_VENT'
  ELSE
    CASE fcd.application_type
    WHEN 'FCON' THEN 'MP_FLARE'
    WHEN 'VCON' THEN 'OTHER_VENT'
    END
  END type
, es.description
, es.metered
, es.comments
FROM envmgr.field_consent_details fcd
JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/REPORT/QUESTION_LIST/QUESTION'
  PASSING
    fcd.xml_data
  COLUMNS
    es_rownum FOR ORDINALITY
  , es_type VARCHAR2(4000) PATH 'TYPE/text()'
  , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
  , metered VARCHAR2(4000) PATH './METERED/text()'
  , comments VARCHAR2(4000) PATH './COMMENTS/text()'
) es
WHERE xfcd.app_length != 'LONG_TERM'
AND coalesce(es.es_type, es.description, es.comments) IS NOT NULL;