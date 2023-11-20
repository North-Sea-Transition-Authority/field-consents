CREATE OR REPLACE VIEW fcs_migration.field_consent_report_data AS
SELECT
  fcd.id fcd_id
, cat.categories
, rd.rd_type
, rd.rd_rownum
, rd.rd_month
, rd.description
, upper(trim(rd.description)) upper_desc
, rd.days
, rd.category_1
, rd.category_2
, rd.category_3
, rd.category_a
, rd.category_b
, rd.category_c
, rd.total_flare_gas
, rd.days_total_shutdown
, rd.comments
FROM envmgr.field_consent_details fcd
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT'
  PASSING
    fcd.xml_data
  COLUMNS
    categories VARCHAR2(4000) PATH './FLAGS/CATEGORIES/text()'
) cat
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/REPORT/REPORT_DATA_LIST/REPORT_DATA'
  PASSING
    fcd.xml_data
  COLUMNS
    rd_type VARCHAR2(4000) PATH 'TYPE/text()' -- MONTH, TOTAL, AVERAGE, INFO
  , rd_rownum FOR ORDINALITY
  , rd_month VARCHAR2(4000) PATH './MONTH/text()'
  , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
  , days INTEGER PATH './DAYS/text()'
  , category_1 NUMBER PATH './CATEGORY_1/text()'
  , category_2 NUMBER PATH './CATEGORY_2/text()'
  , category_3 NUMBER PATH './CATEGORY_3/text()'
  , category_a NUMBER PATH './CATEGORY_A/text()'
  , category_b NUMBER PATH './CATEGORY_B/text()'
  , category_c NUMBER PATH './CATEGORY_C/text()'
  , total_flare_gas NUMBER PATH './TOTAL_FLARE_GAS/text()'
  , days_total_shutdown INTEGER PATH './DAYS_TOTAL_SHUTDOWN/text()'
  , comments VARCHAR2(4000) PATH './COMMENTS/text()'
) rd;