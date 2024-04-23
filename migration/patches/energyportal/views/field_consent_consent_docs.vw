CREATE OR REPLACE VIEW fcs_migration.field_consent_consent_docs AS
SELECT
  fci.id fci_id
, fci.fcd_id
, dim.document_type
, xdd.content_description
, 'document_data_id_'||dd.id dummy_fox_file_id
, CASE dim.document_type
  WHEN 'FC_COVER_LETTER' THEN
    CASE
    WHEN ou.name IS NOT NULL THEN
      'Field Equity Partner Cover Letter ('||ou.name||')'
    ELSE 'Field Equity Partner Cover Letter(s)'
    END
  ELSE xdd.title
  END||'.'||lower(xdd.content_description) filename
, xdd.content_type
, dd.secure_lob_ref.get_size() calculated_file_size
, di.create_wua_id uploaded_by_wua_id
, dd.signed_datetime upload_date_time
, CASE dim.document_type
  WHEN 'FC_COVER_LETTER' THEN
    CASE
    WHEN ou.name IS NOT NULL THEN
      'Field Equity Partner Cover Letter ('||ou.name||')'
    ELSE 'Field Equity Partner Cover Letter(s)'
    END
  ELSE xdd.title
  END file_description
, dd.secure_lob_ref
, dd.secure_lob_ref.get_blob() file_blob_content
FROM envmgr.field_consents_issued fci
JOIN decmgr.xview_document_sets xds ON xds.primary_data_uref = fci.fcd_id||'FC' AND upper(xds.title) = 'FIELD CONSENTS' -- the doc sets without this title are application preview PDFs
JOIN decmgr.xview_document_packs xdp ON xdp.ds_id = xds.ds_id -- this is one package per audience, i.e. applicant (just the consent doc), FEPs (consent doc and cover letter), regulator (audit report, application copy and all supporting docs)
LEFT JOIN decmgr.organisation_units ou ON ou.id = xdp.ou_id
JOIN decmgr.document_instances di ON di.dp_id = xdp.dp_id AND di.copy_of_di_id IS NULL
CROSS JOIN XMLTABLE(
  '/*'
  PASSING
    di.metadata_xml
  COLUMNS
    document_type VARCHAR2(4000) PATH './CONSTRUCTOR_LIST/INCLUDE[1]/NAME/text()'
) dim
JOIN decmgr.document_data dd ON dd.di_id = di.id
JOIN decmgr.xview_document_data xdd on xdd.dd_id = dd.id
WHERE dim.document_type IN ('FC_COVER_LETTER', 'FC_PROD_CONSENT', 'FC_FLARE_CONSENT', 'FC_VENT_CONSENT', 'FC_VENT_CONSENT_SNS_IS')
AND xdd.system_document = 'N';
