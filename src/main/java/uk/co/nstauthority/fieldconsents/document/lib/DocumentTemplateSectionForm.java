package uk.co.nstauthority.fieldconsents.document.lib;

public record DocumentTemplateSectionForm(
    String title,
    String content,
    String conditionMnemonic,
    Boolean numbered,
    Boolean hasPageBreakBefore
) {

  public static DocumentTemplateSectionForm empty() {
    return new DocumentTemplateSectionForm(null, null, null, null, null);
  }

  public static DocumentTemplateSectionForm from(DocumentTemplateSectionDto documentTemplateSectionDto) {
    return new DocumentTemplateSectionForm(
        documentTemplateSectionDto.title(),
        documentTemplateSectionDto.content(),
        documentTemplateSectionDto.conditionMnemonic(),
        documentTemplateSectionDto.numbered(),
        documentTemplateSectionDto.hasPageBreakBefore()
    );
  }
}
