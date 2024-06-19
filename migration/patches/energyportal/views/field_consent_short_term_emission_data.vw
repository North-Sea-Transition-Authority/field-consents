CREATE OR REPLACE VIEW fcs_migration.field_consent_short_term_emission_data AS
WITH base AS (
  SELECT
    fcd.id fcd_id
  , cl.categories
  , cl.short_term_start_date
  , cl.short_term_end_date
  , ed.ed_rownum
  , ed.data_year
  , upper(trim(ed.description)) month
  , ed.days
  , ed.consent_days
  , st.to_number_safe(ed.category_1) category_1
  , st.to_number_safe(ed.category_2) category_2
  , st.to_number_safe(ed.category_3) category_3
  , st.to_number_safe(ed.category_a) category_a
  , st.to_number_safe(ed.category_b) category_b
  , st.to_number_safe(ed.category_c) category_c
  , coalesce(ed.total_flare_gas, ed.total_vent_gas) total_gas
  , clean_text(ed.comments) comments
  FROM envmgr.field_consent_details fcd
  JOIN envmgr.xview_field_consent_details xfcd ON xfcd.fcd_id = fcd.id
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT'
    PASSING
      fcd.xml_data
    COLUMNS
        short_term_start_date DATE PATH 'COVER_INFO/STC_START_DATE/text()'
      , short_term_end_date DATE PATH 'COVER_INFO/STC_END_DATE/text()'
      , categories VARCHAR2(4000) PATH './FLAGS/CATEGORIES/text()'
  ) cl
  CROSS JOIN XMLTABLE(
    '/FIELD_CONSENT/SHORT_TERM_CONSENT/CONSENT_DATA_LIST/CONSENT_DATA[./TYPE/text()="MONTH"]'
    PASSING
      fcd.xml_data
    COLUMNS
      type VARCHAR2(4000) PATH 'TYPE/text()'
    , ed_rownum FOR ORDINALITY
    , data_year INTEGER PATH './DATA_YEAR/text()'
    , description VARCHAR2(4000) PATH './DESCRIPTION/text()'
    , days INTEGER PATH './DAYS/text()'
    , consent_days INTEGER PATH './CONSENT_DAYS/text()'
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
  WHERE xfcd.app_length = 'SHORT_TERM'
)
, base2 AS (
  SELECT b.*
  , to_date('01'||b.month||b.data_year, 'DDMONTHYYYY') month_start_date
  , last_day(to_date('01'||b.month||b.data_year, 'DDMONTHYYYY')) month_end_date
  FROM base b
)
SELECT
  b.fcd_id
, b.categories
, b.ed_rownum
, b.data_year year
, b.month
, b.days
, b.consent_days
, greatest(coalesce(b.short_term_start_date, b.month_start_date), b.month_start_date) start_date
, least(coalesce(b.short_term_end_date, b.month_end_date), b.month_end_date) end_date
, b.category_1
, b.category_2
, b.category_3
, b.category_a
, b.category_b
, b.category_c
, b.total_gas
, b.comments
FROM base2 b;
