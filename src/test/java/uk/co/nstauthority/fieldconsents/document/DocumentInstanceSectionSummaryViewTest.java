package uk.co.nstauthority.fieldconsents.document;

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

    assertThat(DocumentInstanceSectionSummaryView.from(null, documentInstanceSectionDto, "content"))
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo(documentInstanceSectionDto.title());
  }

  @Test
  void titleWithSectionNumber_withSectionNumber() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(DocumentInstanceSectionSummaryView.from("1.2.3", documentInstanceSectionDto, "content"))
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void from_sectionNumbered() {
    var sectionNumberString = "1.2.3";
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var content = "Test content";
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    assertThat(DocumentInstanceSectionSummaryView.from(sectionNumberString, documentInstanceSectionDto, content))
        .isEqualTo(
            new DocumentInstanceSectionSummaryView(
                documentInstanceSectionDto.nestingLevel(),
                sectionNumberString,
                documentInstanceSectionDto.title(),
                content,
                ReverseRouter.route(on(DocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionBefore(documentInstanceSectionId)),
                ReverseRouter.route(on(DocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSectionAfter(documentInstanceSectionId)),
                ReverseRouter.route(on(DocumentInstanceSectionController.class)
                    .getAddDocumentInstanceSubsection(documentInstanceSectionId)),
                ReverseRouter.route(on(DocumentInstanceSectionController.class)
                    .getEditDocumentInstanceSection(documentInstanceSectionId)),
                ReverseRouter.route(on(DocumentInstanceSectionController.class)
                    .getRemoveDocumentInstanceSection(documentInstanceSectionId))
            )
    );
  }
}
