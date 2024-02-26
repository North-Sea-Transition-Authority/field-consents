package uk.co.nstauthority.fieldconsents.document;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceControllerHelperService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSummaryView;

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
        FieldConsentsDocumentInstanceController.class
    );
  }
}
