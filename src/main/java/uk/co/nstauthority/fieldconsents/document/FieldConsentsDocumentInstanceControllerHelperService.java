package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSummaryView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FieldConsentsDocumentInstanceControllerHelperService {

  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;
  private final DocumentInstanceControllerHelperService documentInstanceControllerHelperService;

  FieldConsentsDocumentInstanceControllerHelperService(
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService,
      DocumentInstanceControllerHelperService documentInstanceControllerHelperService
  ) {
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
    this.documentInstanceControllerHelperService = documentInstanceControllerHelperService;
  }

  public List<DocumentInstanceSummaryView> getDocumentInstanceSummaryViews(Application application) {
    var documentInstanceDtos = fieldConsentsDocumentInstanceService.getDocumentInstanceDtos(application);

    return documentInstanceControllerHelperService.getDocumentInstanceSummaryViews(
        documentInstanceDtos,
        documentInstanceDto -> ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceDto.id()))
    );
  }
}
