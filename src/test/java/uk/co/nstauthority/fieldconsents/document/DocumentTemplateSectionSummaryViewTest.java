package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentTemplateSectionSummaryViewTest {

  @Test
  void from_sectionNumbered() {
    var sectionNumberString = "1.2.3";
    var conditionTitle = "Test condition title";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withNumbered(true)
        .build();

    var documentTemplateSectionId = documentTemplateSectionDto.id();

    assertThat(DocumentTemplateSectionSummaryView.from(sectionNumberString, conditionTitle, documentTemplateSectionDto))
        .isEqualTo(
            new DocumentTemplateSectionSummaryView(
                "1.2.3 Test title",
                documentTemplateSectionDto.content(),
                conditionTitle,
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
  void from_sectionNotNumbered() {
    var sectionNumberString = "";
    var conditionTitle = "Test condition title";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withNumbered(false)
        .build();

    var documentTemplateSectionId = documentTemplateSectionDto.id();

    assertThat(DocumentTemplateSectionSummaryView.from(sectionNumberString, conditionTitle, documentTemplateSectionDto))
        .isEqualTo(
            new DocumentTemplateSectionSummaryView(
                "Test title",
                documentTemplateSectionDto.content(),
                conditionTitle,
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
}
