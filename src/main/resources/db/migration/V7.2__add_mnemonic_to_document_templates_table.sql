ALTER TABLE document_library_document_templates ADD COLUMN mnemonic TEXT;

UPDATE document_library_document_templates SET mnemonic = 'PRODUCTION_CONSENT' WHERE title = 'Production Consent';
UPDATE document_library_document_templates SET mnemonic = 'FLARE_CONSENT' WHERE title = 'Flare Consent';
UPDATE document_library_document_templates SET mnemonic = 'VENT_CONSENT' WHERE title = 'Vent Consent';

ALTER TABLE document_library_document_templates ALTER COLUMN mnemonic SET NOT NULL;

ALTER TABLE document_library_document_templates ADD CONSTRAINT doc_lib_doc_template_mnemonic_unq UNIQUE (mnemonic);
