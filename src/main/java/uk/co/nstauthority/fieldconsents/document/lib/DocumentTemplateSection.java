package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "document_library_document_template_sections")
class DocumentTemplateSection {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "document_template_id")
  private DocumentTemplate documentTemplate;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private DocumentTemplateSection parent;

  private String title;

  private String content;

  private String conditionMnemonic;

  private int displayOrder;

  protected DocumentTemplateSection() {
  }

  DocumentTemplateSection(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  DocumentTemplate getDocumentTemplate() {
    return documentTemplate;
  }

  void setDocumentTemplate(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
  }

  DocumentTemplateSection getParent() {
    return parent;
  }

  void setParent(DocumentTemplateSection parent) {
    this.parent = parent;
  }

  String getTitle() {
    return title;
  }

  void setTitle(String title) {
    this.title = title;
  }

  String getContent() {
    return content;
  }

  void setContent(String content) {
    this.content = content;
  }

  String getConditionMnemonic() {
    return conditionMnemonic;
  }

  void setConditionMnemonic(String conditionMnemonic) {
    this.conditionMnemonic = conditionMnemonic;
  }

  int getDisplayOrder() {
    return displayOrder;
  }

  void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
