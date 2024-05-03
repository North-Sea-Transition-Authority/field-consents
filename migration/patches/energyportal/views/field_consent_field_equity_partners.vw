CREATE OR REPLACE VIEW fcs_migration.field_consent_field_equity_partners AS
SELECT
  fci.id fci_id
, fep.organisation_unit_id
, fep.organisation_name
, fep.registered_number
, fep.fep_rownum
FROM envmgr.field_consents_issued fci
CROSS JOIN XMLTABLE(
  '/FIELD_CONSENT/COMPANY_LIST/*'
  PASSING
    fci.xml_data
  COLUMNS
    organisation_unit_id INTEGER PATH './ORGANISATION_UNIT/ID/text()'
  , organisation_name VARCHAR2(4000) PATH './ORGANISATION_UNIT/NAME/text()'
  , registered_number VARCHAR2(4000) PATH './ORGANISATION_UNIT/REGISTERED_NUMBER/text()'
  , fep_rownum FOR ORDINALITY
) fep;
