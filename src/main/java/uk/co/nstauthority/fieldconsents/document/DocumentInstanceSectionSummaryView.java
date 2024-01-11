package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentInstanceSectionSummaryView(
    String title,
    String content,
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {

  static DocumentInstanceSectionSummaryView from(
      String sectionNumberString,
      DocumentInstanceSectionDto documentInstanceSectionDto,
      String content
  ) {
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    return new DocumentInstanceSectionSummaryView(
        "%s %s".formatted(sectionNumberString, documentInstanceSectionDto.title()),
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
    );
  }
}
