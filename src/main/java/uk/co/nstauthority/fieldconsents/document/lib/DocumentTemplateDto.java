package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

public record DocumentTemplateDto(
    UUID id,
    String mnemonic,
    String title,
    String description,
    String templatePath,
    int displayOrder
) {

  static DocumentTemplateDto from(DocumentTemplate documentTemplate) {
    return new DocumentTemplateDto(
        documentTemplate.getId(),
        documentTemplate.getMnemonic(),
        documentTemplate.getTitle(),
        documentTemplate.getDescription(),
        documentTemplate.getTemplatePath(),
        documentTemplate.getDisplayOrder()
    );
  }
}
