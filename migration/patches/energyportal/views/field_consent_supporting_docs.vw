CREATE OR REPLACE VIEW fcs_migration.field_consent_supporting_docs AS
SELECT
  fcd.fcd_id
, fv.id fv_id
, fv.fox_file_id
, aud.filename
, aud.content_type
, aud.file_size
, fv.create_by_wua_id uploaded_by_wua_id
, to_date(aud.upload_date_time, 'YYYY-MM-DD"T"HH24:MI:SS') upload_date_time
, coalesce(aud.description1, aud.description2, aud.filename) file_description
, fv.secure_lob_ref
, fv.secure_lob_ref.get_blob() file_blob_content
, fv.secure_lob_ref.get_size() calculated_file_size
FROM envmgr.xview_field_consent_details fcd
JOIN decmgr.file_folders ff ON ff.id = fcd.folder_id
JOIN decmgr.file_folder_targets fft ON fft.ff_id = ff.id AND fft.status = 'RECEIVED' -- not EMPTY or DELETED
JOIN decmgr.file_versions fv ON fv.fft_id = fft.id AND fv.status = 'RECEIVED' AND fv.status_control = 'C' -- the tip file version
CROSS JOIN XMLTABLE(
  '/file-metadata'
  PASSING
    fv.metadata_xml
  COLUMNS
    filename VARCHAR2(4000) PATH './filename/text()'
  , content_type VARCHAR2(4000) PATH './content-type/text()'
  , file_size INTEGER PATH './size/text()'
  , upload_date_time VARCHAR2(4000) PATH './upload-date-time/text()'
  , description1 VARCHAR2(4000) PATH './description/text()'
  , description2 VARCHAR2(4000) PATH './captured-fields/description/text()'
) aud;
