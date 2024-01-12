package uk.co.nstauthority.fieldconsents.application.caseprocessing.document;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@Controller
@RequestMapping("/applications/{applicationId}/document-preparation")
@ActionEndPoint(CaseProcessingActionItem.DOCUMENT_PREPARATION)
public class DocumentPreparationController {

  private final ApplicationService applicationService;
  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  DocumentPreparationController(
      ApplicationService applicationService,
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService
  ) {
    this.applicationService = applicationService;
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
  }

  @GetMapping
  public ModelAndView viewDocumentInstances(@PathVariable Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSummaryViews = fieldConsentsDocumentInstanceService.getDocumentInstanceSummaryViews(application);

    return new ModelAndView("fcs/application/document/documentPreparation")
        .addObject("pageTitle", "Document preparation")
        .addObject("documentInstanceSummaryViews", documentInstanceSummaryViews);
  }
}
