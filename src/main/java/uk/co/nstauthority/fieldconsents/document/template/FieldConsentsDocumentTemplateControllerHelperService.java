package uk.co.nstauthority.fieldconsents.document.template;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSummaryView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FieldConsentsDocumentTemplateControllerHelperService {

  private final DocumentTemplateControllerHelperService documentTemplateControllerHelperService;

  FieldConsentsDocumentTemplateControllerHelperService(
      DocumentTemplateControllerHelperService documentTemplateControllerHelperService
  ) {
    this.documentTemplateControllerHelperService = documentTemplateControllerHelperService;
  }

  public List<DocumentTemplateSummaryView> getDocumentTemplateSummaryViews() {
    return documentTemplateControllerHelperService.getDocumentTemplateSummaryViews(
        documentTemplateDto -> ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))
    );
  }
}
