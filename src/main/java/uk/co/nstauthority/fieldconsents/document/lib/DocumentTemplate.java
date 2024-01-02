package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "document_library_document_templates")
class DocumentTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String mnemonic;

  private String title;

  private String description;

  private String templatePath;

  private int displayOrder;

  protected DocumentTemplate() {
  }

  DocumentTemplate(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  String getMnemonic() {
    return mnemonic;
  }

  void setMnemonic(String mnemonic) {
    this.mnemonic = mnemonic;
  }

  String getTitle() {
    return title;
  }

  void setTitle(String title) {
    this.title = title;
  }

  String getDescription() {
    return description;
  }

  void setDescription(String description) {
    this.description = description;
  }

  String getTemplatePath() {
    return templatePath;
  }

  void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }

  int getDisplayOrder() {
    return displayOrder;
  }

  void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
