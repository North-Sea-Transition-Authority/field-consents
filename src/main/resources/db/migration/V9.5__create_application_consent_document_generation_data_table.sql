CREATE TABLE application_consent_document_generation_data (
    id                         SERIAL PRIMARY KEY,
    consent_id                 INTEGER NOT NULL REFERENCES application_consents(id),
    document_template_mnemonic TEXT    NOT NULL,
    document_title             TEXT    NOT NULL,
    pdf_html_content           TEXT    NOT NULL,
    pdf_mail_merge_data        JSONB   NOT NULL
);

CREATE INDEX idx_application_consent_document_generation_data_consent_id ON application_consent_document_generation_data(consent_id);
