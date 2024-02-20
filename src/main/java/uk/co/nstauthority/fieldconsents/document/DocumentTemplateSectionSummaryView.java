package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentTemplateSectionSummaryView(
    @Nullable String sectionNumber,
    String title,
    String content,
    @Nullable String conditionTitle,
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

  static DocumentTemplateSectionSummaryView from(
      String sectionNumberString,
      String conditionTitle,
      DocumentTemplateSectionDto documentTemplateSectionDto
  ) {
    var documentTemplateSectionId = documentTemplateSectionDto.id();

    return new DocumentTemplateSectionSummaryView(
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
    );
  }
}
