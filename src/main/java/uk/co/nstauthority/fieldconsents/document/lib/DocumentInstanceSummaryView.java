package uk.co.nstauthority.fieldconsents.document.lib;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record DocumentInstanceSummaryView(
    UUID documentInstanceId,
    String title,
    String description,
    String viewUrl
) {

  static DocumentInstanceSummaryView from(
      DocumentInstanceDto documentInstanceDto,
      Class<? extends DocumentInstanceController> documentInstanceControllerClass
  ) {
    return new DocumentInstanceSummaryView(
        documentInstanceDto.id(),
        documentInstanceDto.title(),
        documentInstanceDto.description(),
        ReverseRouter.route(on(documentInstanceControllerClass).getViewDocumentInstance(documentInstanceDto.id()))
    );
  }
}
