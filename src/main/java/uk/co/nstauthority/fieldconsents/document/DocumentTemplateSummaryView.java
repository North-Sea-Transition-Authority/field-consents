package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentTemplateSummaryView(
    String title,
    String description,
    String viewUrl
) {

  static DocumentTemplateSummaryView from(DocumentTemplateDto documentTemplateDto) {
    return new DocumentTemplateSummaryView(
        documentTemplateDto.title(),
        documentTemplateDto.description(),
        ReverseRouter.route(on(DocumentTemplateController.class).getViewDocumentTemplate(documentTemplateDto.id()))
    );
  }
}
