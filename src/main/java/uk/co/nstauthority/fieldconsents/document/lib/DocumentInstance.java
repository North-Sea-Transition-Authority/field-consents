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
@Table(name = "document_library_document_instances")
class DocumentInstance {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String itemReference;

  private String itemType;

  @ManyToOne
  @JoinColumn(name = "document_template_id")
  private DocumentTemplate documentTemplate;

  protected DocumentInstance() {
  }

  DocumentInstance(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  String getItemReference() {
    return itemReference;
  }

  void setItemReference(String itemReference) {
    this.itemReference = itemReference;
  }

  String getItemType() {
    return itemType;
  }

  void setItemType(String itemType) {
    this.itemType = itemType;
  }

  DocumentTemplate getDocumentTemplate() {
    return documentTemplate;
  }

  void setDocumentTemplate(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
  }
}
