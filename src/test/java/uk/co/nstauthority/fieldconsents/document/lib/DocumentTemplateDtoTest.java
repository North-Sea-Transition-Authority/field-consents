package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTemplateDtoTest {

  @Test
  void from() {
    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    assertThat(DocumentTemplateDto.from(documentTemplate)).isEqualTo(
        new DocumentTemplateDto(
            documentTemplate.getId(),
            documentTemplate.getMnemonic(),
            documentTemplate.getTitle(),
            documentTemplate.getDescription(),
            documentTemplate.getTemplatePath(),
            documentTemplate.getDisplayOrder()
        )
    );
  }
}
