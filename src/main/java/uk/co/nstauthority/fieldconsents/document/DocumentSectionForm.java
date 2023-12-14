package uk.co.nstauthority.fieldconsents.document;

import jakarta.validation.constraints.NotEmpty;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentSectionDto;

public record DocumentSectionForm(
    @NotEmpty(message = "Enter a title") String title,
    String content
) {

  static DocumentSectionForm empty() {
    return new DocumentSectionForm(null, null);
  }

  static DocumentSectionForm from(DocumentSectionDto<?> documentSectionDto) {
    return new DocumentSectionForm(documentSectionDto.title(), documentSectionDto.content());
  }
}
