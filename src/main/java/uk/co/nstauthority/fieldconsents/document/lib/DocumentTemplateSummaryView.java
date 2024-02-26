package uk.co.nstauthority.fieldconsents.document.lib;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentTemplateSummaryView(
    String title,
    String description,
    String viewUrl
) {

  static DocumentTemplateSummaryView from(
      DocumentTemplateDto documentTemplateDto,
      Class<? extends DocumentTemplateController> documentTemplateControllerClass
  ) {
    return new DocumentTemplateSummaryView(
        documentTemplateDto.title(),
        documentTemplateDto.description(),
        ReverseRouter.route(on(documentTemplateControllerClass).getViewDocumentTemplate(documentTemplateDto.id()))
    );
  }
}
