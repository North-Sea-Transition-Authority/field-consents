package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentInstanceSectionFormTest {

  @Test
  void empty() {
    assertThat(DocumentInstanceSectionForm.empty()).isEqualTo(new DocumentInstanceSectionForm(null, null, null));
  }

  @Test
  void from() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    assertThat(DocumentInstanceSectionForm.from(documentInstanceSectionDto)).isEqualTo(
        new DocumentInstanceSectionForm(
            documentInstanceSectionDto.title(),
            documentInstanceSectionDto.content(),
            documentInstanceSectionDto.numbered()
        )
    );
  }
}
