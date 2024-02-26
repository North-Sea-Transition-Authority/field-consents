package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentInstanceSectionSummaryViewTest {

  @Test
  void titleWithSectionNumber_nullSectionNumber() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            null,
            documentInstanceSectionDto,
            "content",
            TestDocumentInstanceSectionController.class
        )
    )
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo(documentInstanceSectionDto.title());
  }

  @Test
  void titleWithSectionNumber_withSectionNumber() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            "1.2.3",
            documentInstanceSectionDto,
            "content",
            TestDocumentInstanceSectionController.class
        )
    )
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void from_sectionNumbered() {
    var sectionNumberString = "1.2.3";
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var content = "Test content";
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            sectionNumberString,
            documentInstanceSectionDto,
            content,
            TestDocumentInstanceSectionController.class
        )
    ).isEqualTo(
        new DocumentInstanceSectionSummaryView(
            documentInstanceSectionDto.nestingLevel(),
            sectionNumberString,
            documentInstanceSectionDto.title(),
            content,
            documentInstanceSectionDto.hasPageBreakBefore(),
            ReverseRouter.route(on(TestDocumentInstanceSectionController.class)
                .getAddDocumentInstanceSectionBefore(documentInstanceSectionId)),
            ReverseRouter.route(on(TestDocumentInstanceSectionController.class)
                .getAddDocumentInstanceSectionAfter(documentInstanceSectionId)),
            ReverseRouter.route(on(TestDocumentInstanceSectionController.class)
                .getAddDocumentInstanceSubsection(documentInstanceSectionId)),
            ReverseRouter.route(on(TestDocumentInstanceSectionController.class)
                .getEditDocumentInstanceSection(documentInstanceSectionId)),
            ReverseRouter.route(on(TestDocumentInstanceSectionController.class)
                .getRemoveDocumentInstanceSection(documentInstanceSectionId))
        )
    );
  }
}
