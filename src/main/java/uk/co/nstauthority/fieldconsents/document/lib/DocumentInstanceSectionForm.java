package uk.co.nstauthority.fieldconsents.document.lib;

public record DocumentInstanceSectionForm(
    String title,
    String content,
    Boolean numbered,
    Boolean hasPageBreakBefore
) {

  public static DocumentInstanceSectionForm empty() {
    return new DocumentInstanceSectionForm(null, null, null, null);
  }

  public static DocumentInstanceSectionForm from(DocumentInstanceSectionDto documentInstanceSectionDto) {
    return new DocumentInstanceSectionForm(
        documentInstanceSectionDto.title(),
        documentInstanceSectionDto.content(),
        documentInstanceSectionDto.numbered(),
        documentInstanceSectionDto.hasPageBreakBefore()
    );
  }
}
