package uk.co.nstauthority.fieldconsents.document;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;

public record DocumentInstanceSectionForm(
    String title,
    String content,
    Boolean numbered,
    Boolean hasPageBreakBefore
) {

  static DocumentInstanceSectionForm empty() {
    return new DocumentInstanceSectionForm(null, null, null, null);
  }

  static DocumentInstanceSectionForm from(DocumentInstanceSectionDto documentInstanceSectionDto) {
    return new DocumentInstanceSectionForm(
        documentInstanceSectionDto.title(),
        documentInstanceSectionDto.content(),
        documentInstanceSectionDto.numbered(),
        documentInstanceSectionDto.hasPageBreakBefore()
    );
  }
}
