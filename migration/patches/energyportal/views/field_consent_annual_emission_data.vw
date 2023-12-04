CREATE OR REPLACE VIEW fcs_migration.field_consent_annual_emission_data AS
SELECT
  fcd.id fcd_id
, cat.categories
, ed.ed_rownum
, xfcd.application_year year
, upper(trim(ed.description)) month
, ed.days
, st.to_number_safe(ed.category_1) category_1
, st.to_number_safe(ed.category_2) category_2
, st.to_number_safe(ed.category_3) category_3
, st.to_number_safe(ed.category_a) category_a
, st.to_number_safe(ed.category_b) category_b
, st.to_number_safe(ed.category_c) category_c
, coalesce(ed.total_flare_gas, ed.total_vent_gas) total_gas
, ed.comments
FROM envmgr.field_consent_details fcd
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
  , ed_rownum FOR ORDINALITY
  , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
  , days INTEGER PATH './DAYS/text()'
  , category_1 VARCHAR2(4000) PATH './CATEGORY_1/text()'
  , category_2 VARCHAR2(4000) PATH './CATEGORY_2/text()'
  , category_3 VARCHAR2(4000) PATH './CATEGORY_3/text()'
  , category_a VARCHAR2(4000) PATH './CATEGORY_A/text()'
  , category_b VARCHAR2(4000) PATH './CATEGORY_B/text()'
  , category_c VARCHAR2(4000) PATH './CATEGORY_C/text()'
  , total_flare_gas NUMBER PATH './TOTAL_FLARE_GAS/text()'
  , total_vent_gas NUMBER PATH './TOTAL_VENT_GAS/text()'
  , comments VARCHAR2(4000) PATH './COMMENTS/text()'
) ed
WHERE xfcd.app_length = 'ANNUAL';