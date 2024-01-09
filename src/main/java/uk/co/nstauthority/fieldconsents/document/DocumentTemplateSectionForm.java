package uk.co.nstauthority.fieldconsents.document;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;

public record DocumentTemplateSectionForm(
    String title,
    String content,
    String conditionMnemonic
) {

  static DocumentTemplateSectionForm empty() {
    return new DocumentTemplateSectionForm(null, null, null);
  }

  static DocumentTemplateSectionForm from(DocumentTemplateSectionDto documentTemplateSectionDto) {
    return new DocumentTemplateSectionForm(
        documentTemplateSectionDto.title(),
        documentTemplateSectionDto.content(),
        documentTemplateSectionDto.conditionMnemonic()
    );
  }
}
