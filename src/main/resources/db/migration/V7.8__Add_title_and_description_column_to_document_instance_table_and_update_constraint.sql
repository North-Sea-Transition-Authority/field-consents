ALTER TABLE document_library_document_instances
    ADD COLUMN title TEXT,
    ADD COLUMN description TEXT;

UPDATE document_library_document_instances
SET title = 'Production Consent',
    description = 'Production Consent document for this application'
WHERE item_type = 'PRODUCTION_CONSENT';

UPDATE document_library_document_instances
SET title = 'Vent Consent',
    description = 'Vent Consent document for this application'
WHERE item_type = 'VENT_CONSENT';

UPDATE document_library_document_instances
SET title = 'Flare Consent',
    description = 'Flare Consent document for this application'
WHERE item_type = 'FLARE_CONSENT';

ALTER TABLE document_library_document_instances
    ALTER COLUMN title SET NOT NULL,
    ALTER COLUMN description SET NOT NULL;

ALTER TABLE document_library_document_instances
    DROP CONSTRAINT doc_lib_doc_instances_item_reference_item_type_unq,
    ADD CONSTRAINT doc_lib_doc_instances_item_ref_item_type_doc_template_unq UNIQUE (item_reference, item_type, document_template_id);
