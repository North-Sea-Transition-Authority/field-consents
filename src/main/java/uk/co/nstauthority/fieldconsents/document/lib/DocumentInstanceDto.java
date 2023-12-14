package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

public record DocumentInstanceDto(
    UUID id,
    String itemReference,
    String itemType,
    DocumentTemplateDto documentTemplateDto
) {

  static DocumentInstanceDto from(DocumentInstance documentInstance) {
    return new DocumentInstanceDto(
        documentInstance.getId(),
        documentInstance.getItemReference(),
        documentInstance.getItemType(),
        DocumentTemplateDto.from(documentInstance.getDocumentTemplate())
    );
  }
}
