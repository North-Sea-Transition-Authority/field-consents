package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-preparation")
@ActionEndPoint(CaseProcessingActionItem.CONSENT_PREPARATION)
public class ConsentPreparationController {

  private final ApplicationService applicationService;
  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;
  private final ConsentDataService consentDataService;

  ConsentPreparationController(
      ApplicationService applicationService,
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService,
      ConsentDataService consentDataService
  ) {
    this.applicationService = applicationService;
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
    this.consentDataService = consentDataService;
  }

  @GetMapping
  public ModelAndView viewDocumentInstances(@PathVariable Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    return consentDataService.findConsentData(application)
        .map(ConsentDataView::from)
        .map(consentDataView -> getModelAndView(application, consentDataView))
        .orElse(ReverseRouter.redirect(on(ConsentDataController.class).editConsentData(applicationId)));
  }

  private ModelAndView getModelAndView(
      Application application,
      ConsentDataView consentDataView
  ) {
    var applicationId = application.getId();
    var documentInstanceSummaryViews = fieldConsentsDocumentInstanceService.getDocumentInstanceSummaryViews(application);

    return new ModelAndView("fcs/application/consent/consentPreparation")
        .addObject("pageTitle", "Consent preparation")
        .addObject("documentInstanceSummaryViews", documentInstanceSummaryViews)
        .addObject("consentDataView", consentDataView)
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)))
        .addObject("consentDataEditUrl", ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(applicationId)));
  }

}
