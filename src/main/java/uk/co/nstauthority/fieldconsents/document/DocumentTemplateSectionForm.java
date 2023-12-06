package uk.co.nstauthority.fieldconsents.document;

import jakarta.validation.constraints.NotEmpty;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;

public record DocumentTemplateSectionForm(
    @NotEmpty(message = "Enter a title") String title,
    String content
) {

  static DocumentTemplateSectionForm empty() {
    return new DocumentTemplateSectionForm(null, null);
  }

  static DocumentTemplateSectionForm from(DocumentTemplateSectionDto documentTemplateSectionDto) {
    return new DocumentTemplateSectionForm(documentTemplateSectionDto.title(), documentTemplateSectionDto.content());
  }
}
