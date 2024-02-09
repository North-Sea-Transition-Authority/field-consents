package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_PREPARATION;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentsController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-preparation")
@ActionEndPoint(CONSENT_PREPARATION)
public class ConsentPreparationController {

  private final ApplicationVersionService applicationVersionService;
  private final ConsentDataService consentDataService;
  private final ConsentFigureUnitService consentFigureUnitService;
  private final ConsentLengthService consentLengthService;
  private final ConsentPreparationDocumentService consentPreparationDocumentService;
  private final FieldEquityPartnerService fieldEquityPartnerService;
  private final ApplicationAssetService applicationAssetService;

  ConsentPreparationController(
      ApplicationVersionService applicationVersionService,
      ConsentDataService consentDataService,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentLengthService consentLengthService,
      ConsentPreparationDocumentService consentPreparationDocumentService,
      FieldEquityPartnerService fieldEquityPartnerService,
      ApplicationAssetService applicationAssetService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.consentDataService = consentDataService;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentLengthService = consentLengthService;
    this.consentPreparationDocumentService = consentPreparationDocumentService;
    this.fieldEquityPartnerService = fieldEquityPartnerService;
    this.applicationAssetService = applicationAssetService;
  }

  @GetMapping
  public ModelAndView viewConsentPreparationPage(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

    return consentDataService.findConsentData(applicationVersion.getApplication())
        .map(consentData -> consentDataService.getConsentDataView(applicationVersion, consentData, consentLengthType))
        .map(consentDataView -> getModelAndView(applicationVersion, consentDataView, consentLengthType))
        .orElse(ReverseRouter.redirect(on(ConsentDataController.class).editConsentData(applicationId)));
  }

  private ModelAndView getModelAndView(
      ApplicationVersion applicationVersion,
      ConsentDataView consentDataView,
      ConsentLengthType consentLengthType
  ) {
    var application = applicationVersion.getApplication();
    var applicationId = application.getId();

    var modelAndView = new ModelAndView("fcs/application/consent/consentPreparation")
        .addObject("pageTitle", CONSENT_PREPARATION.getDisplayName())
        .addObject("applicationType", application.getType())
        .addObject("consentLengthType", consentLengthType)
        .addObject("consentDocumentsSummaryCard", consentPreparationDocumentService.getConsentDocumentsSummaryCard(application))
        .addObject("consentDataView", consentDataView)
        .addObject(
            "consentFigureUnitView",
            consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType)
        )
        .addObject("consentDataEditUrl", ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(applicationId)))
        .addObject("consentDocumentsEditUrl", ReverseRouter.route(on(ConsentPreparationDocumentsController.class)
            .editDocuments(applicationId)))
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));

    if (applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      var fieldEquityPartnersView = fieldEquityPartnerService.getFieldEquityPartnersView(applicationVersion);
      modelAndView.addObject("fieldEquityPartnersView", fieldEquityPartnersView);
    }

    return modelAndView;
  }
}
