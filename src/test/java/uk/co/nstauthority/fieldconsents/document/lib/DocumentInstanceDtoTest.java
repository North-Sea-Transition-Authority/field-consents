package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentInstanceDtoTest {

  @Test
  void from() {
    var documentInstance = DocumentInstanceTestUtil.builder().build();

    assertThat(DocumentInstanceDto.from(documentInstance)).isEqualTo(
        new DocumentInstanceDto(
            documentInstance.getId(),
            documentInstance.getItemReference(),
            documentInstance.getItemType(),
            DocumentTemplateDto.from(documentInstance.getDocumentTemplate())
        )
    );
  }
}
