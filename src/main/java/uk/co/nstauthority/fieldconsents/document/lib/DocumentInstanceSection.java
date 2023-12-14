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
@Table(name = "document_library_document_instance_sections")
class DocumentInstanceSection {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "document_instance_id")
  private DocumentInstance documentInstance;

  @ManyToOne
  @JoinColumn(name = "created_from_document_template_section_id")
  private DocumentTemplateSection createdFromDocumentTemplateSection;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private DocumentInstanceSection parent;

  private String title;

  private String content;

  private int displayOrder;

  protected DocumentInstanceSection() {
  }

  DocumentInstanceSection(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  DocumentInstance getDocumentInstance() {
    return documentInstance;
  }

  void setDocumentInstance(DocumentInstance documentInstance) {
    this.documentInstance = documentInstance;
  }

  DocumentTemplateSection getCreatedFromDocumentTemplateSection() {
    return createdFromDocumentTemplateSection;
  }

  void setCreatedFromDocumentTemplateSection(DocumentTemplateSection createdFromDocumentTemplateSection) {
    this.createdFromDocumentTemplateSection = createdFromDocumentTemplateSection;
  }

  DocumentInstanceSection getParent() {
    return parent;
  }

  void setParent(DocumentInstanceSection parent) {
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

  int getDisplayOrder() {
    return displayOrder;
  }

  void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
