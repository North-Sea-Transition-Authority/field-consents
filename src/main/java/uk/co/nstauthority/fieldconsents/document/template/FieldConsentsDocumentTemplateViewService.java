package uk.co.nstauthority.fieldconsents.document.template;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateViewService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FieldConsentsDocumentTemplateViewService {

  private final DocumentTemplateViewService documentTemplateViewService;

  FieldConsentsDocumentTemplateViewService(DocumentTemplateViewService documentTemplateViewService) {
    this.documentTemplateViewService = documentTemplateViewService;
  }

  public List<DocumentTemplateSummaryView> getDocumentTemplateSummaryViews() {
    return documentTemplateViewService.getDocumentTemplateSummaryViews(
        documentTemplateDto -> ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))
    );
  }
}
