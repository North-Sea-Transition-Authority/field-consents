ALTER TABLE document_library_document_template_sections ADD COLUMN numbered BOOLEAN;
UPDATE document_library_document_template_sections SET numbered = TRUE;
ALTER TABLE document_library_document_template_sections ALTER COLUMN numbered SET NOT NULL;

ALTER TABLE document_library_document_instance_sections ADD COLUMN numbered BOOLEAN;
UPDATE document_library_document_instance_sections SET numbered = TRUE;
ALTER TABLE document_library_document_instance_sections ALTER COLUMN numbered SET NOT NULL;
