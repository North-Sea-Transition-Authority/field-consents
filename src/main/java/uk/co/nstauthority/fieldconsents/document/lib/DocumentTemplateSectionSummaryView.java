package uk.co.nstauthority.fieldconsents.document.lib;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
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
      DocumentTemplateSectionDto documentTemplateSectionDto,
      Class<? extends DocumentTemplateSectionController> documentTemplateSectionControllerClass
  ) {
    var documentTemplateSectionId = documentTemplateSectionDto.id();

    return new DocumentTemplateSectionSummaryView(
        sectionNumberString,
        documentTemplateSectionDto.title(),
        documentTemplateSectionDto.content(),
        conditionTitle,
        documentTemplateSectionDto.hasPageBreakBefore(),
        ReverseRouter.route(on(documentTemplateSectionControllerClass)
            .getAddDocumentTemplateSectionBefore(documentTemplateSectionId)),
        ReverseRouter.route(on(documentTemplateSectionControllerClass)
            .getAddDocumentTemplateSectionAfter(documentTemplateSectionId)),
        ReverseRouter.route(on(documentTemplateSectionControllerClass)
            .getAddDocumentTemplateSubsection(documentTemplateSectionId)),
        ReverseRouter.route(on(documentTemplateSectionControllerClass)
            .getEditDocumentTemplateSection(documentTemplateSectionId)),
        ReverseRouter.route(on(documentTemplateSectionControllerClass)
            .getRemoveDocumentTemplateSection(documentTemplateSectionId))
    );
  }
}
