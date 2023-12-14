package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentSectionSummaryView(
    String title,
    String content,
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {

  static DocumentSectionSummaryView from(
      String sectionNumberString,
      DocumentSectionDto<?> documentSectionDto
  ) {
    var title = "%s %s".formatted(sectionNumberString, documentSectionDto.title());

    String addSectionBeforeUrl;
    String addSectionAfterUrl;
    String addSubsectionUrl;
    String editUrl;
    String removeUrl;

    if (documentSectionDto instanceof DocumentTemplateSectionDto) {
      var documentSectionId = documentSectionDto.id();

      addSectionBeforeUrl = ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getAddDocumentTemplateSectionBefore(documentSectionId));
      addSectionAfterUrl =  ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getAddDocumentTemplateSectionAfter(documentSectionId));
      addSubsectionUrl = ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getAddDocumentTemplateSubsection(documentSectionId));
      editUrl = ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getEditDocumentTemplateSection(documentSectionId));
      removeUrl = ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getRemoveDocumentTemplateSection(documentSectionId));
    } else if (documentSectionDto instanceof DocumentInstanceSectionDto) {
      addSectionBeforeUrl = null; // TODO FCS-537
      addSectionAfterUrl = null; // TODO FCS-537
      addSubsectionUrl = null; // TODO FCS-537
      editUrl = null; // TODO FCS-537
      removeUrl = null; // TODO FCS-537
    } else {
      throw new IllegalStateException("Unknown DocumentSectionDto class %s".formatted(documentSectionDto.getClass()));
    }

    return new DocumentSectionSummaryView(
        title,
        documentSectionDto.content(),
        addSectionBeforeUrl,
        addSectionAfterUrl,
        addSubsectionUrl,
        editUrl,
        removeUrl
    );
  }
}
