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
, st.to_number_safe(rd.category_1) category_1
, st.to_number_safe(rd.category_2) category_2
, st.to_number_safe(rd.category_3) category_3
, st.to_number_safe(rd.category_a) category_a
, st.to_number_safe(rd.category_b) category_b
, st.to_number_safe(rd.category_c) category_c
, rd.total_flare_gas
, rd.days_total_shutdown
-- replace multibyte characters as they cause issues with the db link push:
-- � and �
, replace(replace(rd.comments, CHR(191), NULL), CHR(183), '-') comments
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
  , category_1 VARCHAR2(4000) PATH './CATEGORY_1/text()'
  , category_2 VARCHAR2(4000) PATH './CATEGORY_2/text()'
  , category_3 VARCHAR2(4000) PATH './CATEGORY_3/text()'
  , category_a VARCHAR2(4000) PATH './CATEGORY_A/text()'
  , category_b VARCHAR2(4000) PATH './CATEGORY_B/text()'
  , category_c VARCHAR2(4000) PATH './CATEGORY_C/text()'
  , total_flare_gas NUMBER PATH './TOTAL_FLARE_GAS/text()'
  , days_total_shutdown INTEGER PATH './DAYS_TOTAL_SHUTDOWN/text()'
  , comments VARCHAR2(4000) PATH './COMMENTS/text()'
) rd;
