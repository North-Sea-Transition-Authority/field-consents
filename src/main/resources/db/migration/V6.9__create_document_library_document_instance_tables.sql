CREATE TABLE document_library_document_instances(
  id UUID NOT NULL
, item_reference TEXT NOT NULL
, item_type TEXT NOT NULL
, document_template_id UUID NOT NULL
, CONSTRAINT doc_lib_doc_instances_pk PRIMARY KEY (id)
, CONSTRAINT doc_lib_doc_instances_item_reference_item_type_unq UNIQUE (item_reference, item_type)
, CONSTRAINT doc_lib_doc_instances_document_template_id_fk FOREIGN KEY (document_template_id) REFERENCES document_library_document_templates (id)
);

CREATE INDEX doc_lib_doc_instances_doc_template_id_idx ON document_library_document_instances (document_template_id);

CREATE TABLE document_library_document_instance_sections(
  id UUID NOT NULL
, document_instance_id UUID NOT NULL
, created_from_document_template_section_id UUID -- There is intentionally no foreign key on this column as template sections are deletable.
, parent_id UUID
, title TEXT NOT NULL
, content TEXT
, display_order INTEGER NOT NULL
, CONSTRAINT doc_lib_doc_instance_sections_pk PRIMARY KEY (id)
, CONSTRAINT doc_lib_doc_instance_sections_doc_instance_id_fk FOREIGN KEY (document_instance_id) REFERENCES document_library_document_instances (id)
, CONSTRAINT doc_lib_doc_instance_sections_parent_id_fk FOREIGN KEY (parent_id) REFERENCES document_library_document_instance_sections (id)
-- DEFERRABLE INITIALLY DEFERRED is required for DocumentInstanceSectionService#createDocumentInstanceSection to avoid a
-- ConstraintViolationException when shifting existing section display orders.
, CONSTRAINT doc_lib_doc_instance_sections_parent_id_display_order_unq UNIQUE (parent_id, display_order) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX doc_lib_doc_instance_sections_doc_instance_id_idx ON document_library_document_instance_sections (document_instance_id);

CREATE INDEX doc_lib_doc_instance_sections_parent_id_idx ON document_library_document_instance_sections (parent_id);
