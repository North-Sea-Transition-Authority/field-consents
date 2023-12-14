package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentSectionSummaryViewTest {

  @Test
  void from_withDocumentTemplateSectionDto() {
    var sectionNumberString = "1.2.3";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    var documentTemplateSectionId = documentTemplateSectionDto.id();

    assertThat(DocumentSectionSummaryView.from(sectionNumberString, documentTemplateSectionDto)).isEqualTo(
        new DocumentSectionSummaryView(
            "1.2.3 Test title",
            documentTemplateSectionDto.content(),
            ReverseRouter.route(on(DocumentTemplateSectionController.class)
                .getAddDocumentTemplateSectionBefore(documentTemplateSectionId)),
            ReverseRouter.route(on(DocumentTemplateSectionController.class)
                .getAddDocumentTemplateSectionAfter(documentTemplateSectionId)),
            ReverseRouter.route(on(DocumentTemplateSectionController.class)
                .getAddDocumentTemplateSubsection(documentTemplateSectionId)),
            ReverseRouter.route(on(DocumentTemplateSectionController.class)
                .getEditDocumentTemplateSection(documentTemplateSectionId)),
            ReverseRouter.route(on(DocumentTemplateSectionController.class)
                .getRemoveDocumentTemplateSection(documentTemplateSectionId))
        )
    );
  }

  @Test
  void from_withDocumentInstanceSectionDto() {
    var sectionNumberString = "1.2.3";

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    assertThat(DocumentSectionSummaryView.from(sectionNumberString, documentInstanceSectionDto)).isEqualTo(
        new DocumentSectionSummaryView(
            "1.2.3 Test title",
            documentInstanceSectionDto.content(),
            null,
            null,
            null,
            null,
            null
        )
    );
  }
}
