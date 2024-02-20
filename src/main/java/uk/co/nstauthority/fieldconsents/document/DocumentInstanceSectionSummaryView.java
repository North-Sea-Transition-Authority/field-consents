package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentInstanceSectionSummaryView(
    int nestingLevel,
    @Nullable String sectionNumber,
    String title,
    String content,
    boolean hasPageBreakBefore,
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {

  public String titleWithSectionNumber() {
    if (sectionNumber == null) {
      return title;
    }

    return "%s %s".formatted(sectionNumber, title);
  }

  static DocumentInstanceSectionSummaryView from(
      String sectionNumberString,
      DocumentInstanceSectionDto documentInstanceSectionDto,
      String content
  ) {
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    return new DocumentInstanceSectionSummaryView(
        documentInstanceSectionDto.nestingLevel(),
        sectionNumberString,
        documentInstanceSectionDto.title(),
        content,
        documentInstanceSectionDto.hasPageBreakBefore(),
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
