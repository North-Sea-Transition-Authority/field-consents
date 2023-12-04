CREATE TABLE document_library_document_templates(
  id UUID PRIMARY KEY NOT NULL
, title TEXT NOT NULL
, description TEXT NOT NULL
, template_path TEXT NOT NULL
, display_order INTEGER NOT NULL
, CONSTRAINT display_order_unique UNIQUE (display_order)
);

CREATE TABLE document_library_document_template_sections(
  id UUID PRIMARY KEY NOT NULL
, document_template_id UUID NOT NULL
, parent_id UUID
, title TEXT NOT NULL
, content TEXT
, display_order INTEGER NOT NULL
, CONSTRAINT document_template_id_fk FOREIGN KEY (document_template_id) REFERENCES document_library_document_templates (id)
, CONSTRAINT parent_id_id_fk FOREIGN KEY (parent_id) REFERENCES document_library_document_template_sections (id)
, CONSTRAINT parent_id_display_order_unique UNIQUE (parent_id, display_order)
);

CREATE INDEX document_library_document_template_sections_doc_template_id_idx ON document_library_document_template_sections (document_template_id);

CREATE INDEX document_library_document_template_sections_parent_id_idx ON document_library_document_template_sections (parent_id);
