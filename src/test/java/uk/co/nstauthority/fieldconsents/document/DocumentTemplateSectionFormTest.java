package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTemplateSectionFormTest {

  @Test
  void empty() {
    assertThat(DocumentTemplateSectionForm.empty()).isEqualTo(new DocumentTemplateSectionForm(null, null, null, null, null));
  }

  @Test
  void from() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    assertThat(DocumentTemplateSectionForm.from(documentTemplateSectionDto)).isEqualTo(
        new DocumentTemplateSectionForm(
            documentTemplateSectionDto.title(),
            documentTemplateSectionDto.content(),
            documentTemplateSectionDto.conditionMnemonic(),
            documentTemplateSectionDto.numbered(),
            documentTemplateSectionDto.hasPageBreakBefore()
        )
    );
  }
}
