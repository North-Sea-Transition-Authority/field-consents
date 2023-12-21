package uk.co.nstauthority.fieldconsents.document;

import jakarta.validation.constraints.NotEmpty;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;

public record DocumentInstanceSectionForm(
    @NotEmpty(message = "Enter a title") String title,
    String content
) {

  static DocumentInstanceSectionForm empty() {
    return new DocumentInstanceSectionForm(null, null);
  }

  static DocumentInstanceSectionForm from(DocumentInstanceSectionDto documentInstanceSectionDto) {
    return new DocumentInstanceSectionForm(documentInstanceSectionDto.title(), documentInstanceSectionDto.content());
  }
}
