package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_PREPARATION;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentsController;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-preparation")
@ActionEndPoint(CONSENT_PREPARATION)
public class ConsentPreparationController {

  private final ApplicationService applicationService;
  private final ConsentDataService consentDataService;
  private final ConsentPreparationDocumentService consentPreparationDocumentService;

  ConsentPreparationController(
      ApplicationService applicationService,
      ConsentDataService consentDataService,
      ConsentPreparationDocumentService consentPreparationDocumentService
  ) {
    this.applicationService = applicationService;
    this.consentDataService = consentDataService;
    this.consentPreparationDocumentService = consentPreparationDocumentService;
  }

  @GetMapping
  public ModelAndView viewConsentPreparationPage(@PathVariable Integer applicationId) {
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

    return new ModelAndView("fcs/application/consent/consentPreparation")
        .addObject("pageTitle", CONSENT_PREPARATION.getDisplayName())
        .addObject("consentDocumentsSummaryCard", consentPreparationDocumentService.getConsentDocumentsSummaryCard(application))
        .addObject("consentDataView", consentDataView)
        .addObject("consentDataEditUrl", ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(applicationId)))
        .addObject("consentDocumentsEditUrl", ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
            .editDocuments(applicationId)))
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));

  }

}
