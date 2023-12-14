package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentSectionFormTest {

  @Test
  void empty() {
    assertThat(DocumentSectionForm.empty()).isEqualTo(new DocumentSectionForm(null, null));
  }

  @Test
  void from() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    assertThat(DocumentSectionForm.from(documentTemplateSectionDto)).isEqualTo(
        new DocumentSectionForm(documentTemplateSectionDto.title(), documentTemplateSectionDto.content())
    );
  }
}
