CREATE OR REPLACE VIEW fcs_migration.field_consent_intentions AS
SELECT
  fcd.id fcd_id
, xis.is_id
, xid.in_id
, xid.id_id
, xid.created_by_wua_id
, xid.created_datetime
, xid.class_type
, xid.severity
, XMLQUERY('/CLAUSE_TEXT/node()' PASSING xid.clause_text RETURNING CONTENT).getClobVal() intention_html
FROM envmgr.field_consent_details fcd
JOIN bpmmgr.xview_intention_sets xis ON xis.primary_data_uref = fcd.id||'FC'
JOIN bpmmgr.intention_set_intentions isi ON isi.is_id = xis.is_id AND isi.end_datetime IS NULL
JOIN bpmmgr.intentions i ON i.id = isi.in_id
JOIN bpmmgr.xview_intention_details xid ON xid.in_id = i.id
WHERE xid.clause_type = 'FIELD_CONSENTS'
AND xid.end_datetime IS NULL
-- remove the duplicates across the sets (for the app versions within a variation)
AND i.original_id_id IS NULL
AND XMLQUERY('/CLAUSE_TEXT/node()' PASSING xid.clause_text RETURNING CONTENT).getClobVal() IS NOT NULL;