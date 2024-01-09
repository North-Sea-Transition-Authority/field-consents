package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentTemplateSectionSummaryView(
    String title,
    String content,
    @Nullable String conditionTitle,
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {

  static DocumentTemplateSectionSummaryView from(
      String sectionNumberString,
      String conditionTitle,
      DocumentTemplateSectionDto documentTemplateSectionDto
  ) {
    var documentTemplateSectionId = documentTemplateSectionDto.id();

    return new DocumentTemplateSectionSummaryView(
        "%s %s".formatted(sectionNumberString, documentTemplateSectionDto.title()),
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
    );
  }
}
