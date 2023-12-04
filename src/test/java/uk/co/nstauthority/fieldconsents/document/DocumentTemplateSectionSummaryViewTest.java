package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTemplateSectionSummaryViewTest {

  @Test
  void from() {
    var sectionNumberString = "1.2.3";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    assertThat(DocumentTemplateSectionSummaryView.from(sectionNumberString, documentTemplateSectionDto))
        .isEqualTo(
            new DocumentTemplateSectionSummaryView("1.2.3 Test title", documentTemplateSectionDto.content())
        );
  }
}
