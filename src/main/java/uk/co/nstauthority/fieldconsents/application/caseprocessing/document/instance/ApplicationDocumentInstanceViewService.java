package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceViewService;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class ApplicationDocumentInstanceViewService {

  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;
  private final DocumentInstanceViewService documentInstanceViewService;

  ApplicationDocumentInstanceViewService(
      ApplicationDocumentInstanceService applicationDocumentInstanceService,
      DocumentInstanceViewService documentInstanceViewService
  ) {
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
    this.documentInstanceViewService = documentInstanceViewService;
  }

  public List<DocumentInstanceSummaryView> getDocumentInstanceSummaryViews(Application application) {
    var documentInstanceDtos = applicationDocumentInstanceService.getDocumentInstanceDtos(application);

    return documentInstanceViewService.getDocumentInstanceSummaryViews(
        documentInstanceDtos,
        documentInstanceDto -> ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(application.getId(), documentInstanceDto.id()))
    );
  }
}
