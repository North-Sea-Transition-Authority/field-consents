package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentTemplateSectionSummaryViewTest {

  @Test
  void titleWithSectionNumber_nullSectionNumber() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(DocumentTemplateSectionSummaryView.from(null, "condition title", documentTemplateSectionDto))
        .extracting(DocumentTemplateSectionSummaryView::titleWithSectionNumber)
        .isEqualTo(documentTemplateSectionDto.title());
  }

  @Test
  void titleWithSectionNumber_withSectionNumber() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(DocumentTemplateSectionSummaryView.from("1.2.3", "condition title", documentTemplateSectionDto))
        .extracting(DocumentTemplateSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void from() {
    var sectionNumberString = "1.2.3";
    var conditionTitle = "Test condition title";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionId = documentTemplateSectionDto.id();

    assertThat(DocumentTemplateSectionSummaryView.from(sectionNumberString, conditionTitle, documentTemplateSectionDto))
        .isEqualTo(
            new DocumentTemplateSectionSummaryView(
                sectionNumberString,
                documentTemplateSectionDto.title(),
                documentTemplateSectionDto.content(),
                conditionTitle,
                documentTemplateSectionDto.hasPageBreakBefore(),
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
