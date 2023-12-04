package uk.co.nstauthority.fieldconsents.document;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;

public record DocumentTemplateSectionSummaryView(
    String title,
    String content
) {

  static DocumentTemplateSectionSummaryView from(
      String sectionNumberString,
      DocumentTemplateSectionDto documentTemplateSectionDto
  ) {
    return new DocumentTemplateSectionSummaryView(
        "%s %s".formatted(sectionNumberString, documentTemplateSectionDto.title()),
        documentTemplateSectionDto.content()
    );
  }
}
