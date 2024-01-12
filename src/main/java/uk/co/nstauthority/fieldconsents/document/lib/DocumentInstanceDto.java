package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

public record DocumentInstanceDto(
    UUID id,
    String itemReference,
    String itemType,
    String title,
    String description,
    DocumentTemplateDto documentTemplateDto
) {

  static DocumentInstanceDto from(DocumentInstance documentInstance) {
    return new DocumentInstanceDto(
        documentInstance.getId(),
        documentInstance.getItemReference(),
        documentInstance.getItemType(),
        documentInstance.getTitle(),
        documentInstance.getDescription(),
        DocumentTemplateDto.from(documentInstance.getDocumentTemplate())
    );
  }
}
