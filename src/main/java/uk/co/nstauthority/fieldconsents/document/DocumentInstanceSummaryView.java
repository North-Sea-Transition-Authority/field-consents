package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentInstanceSummaryView(String title, String description, String viewUrl) {

  static DocumentInstanceSummaryView from(DocumentInstanceDto documentInstanceDto) {
    return new DocumentInstanceSummaryView(
        documentInstanceDto.title(),
        documentInstanceDto.description(),
        ReverseRouter.route(on(DocumentInstanceController.class).getViewDocumentInstance(documentInstanceDto.id()))
    );
  }

}
