package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_PREPARATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_CONSENT_DATA;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

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
  private final CaseProcessingActionService caseProcessingActionService;
  private final CaseStatusFlagService caseStatusFlagService;

  ConsentPreparationController(
      ApplicationVersionService applicationVersionService,
      ConsentDataService consentDataService,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentLengthService consentLengthService,
      ConsentPreparationDocumentService consentPreparationDocumentService,
      FieldEquityPartnerService fieldEquityPartnerService,
      ApplicationAssetService applicationAssetService,
      CaseProcessingActionService caseProcessingActionService,
      CaseStatusFlagService caseStatusFlagService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.consentDataService = consentDataService;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentLengthService = consentLengthService;
    this.consentPreparationDocumentService = consentPreparationDocumentService;
    this.fieldEquityPartnerService = fieldEquityPartnerService;
    this.applicationAssetService = applicationAssetService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.caseStatusFlagService = caseStatusFlagService;
  }

  @GetMapping
  public ModelAndView viewConsentPreparationPage(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var consentDataOptional = consentDataService.findConsentData(application);

    if (consentDataOptional.isEmpty()) {
      var hasEditConsentDataAction = caseProcessingActionService.getUserActionItems(applicationVersion, user)
          .contains(EDIT_CONSENT_DATA);
      if (hasEditConsentDataAction) {
        return ReverseRouter.redirect(on(ConsentDataController.class).editConsentData(applicationId));
      }
    }

    return getModelAndViewWithConsentData(applicationVersion, consentDataOptional.orElse(null), user);
  }

  private ModelAndView getModelAndViewWithConsentData(
      ApplicationVersion applicationVersion,
      @Nullable ConsentData consentData,
      ServiceUserDetail user
  ) {
    var application = applicationVersion.getApplication();

    var consentPreparationGroupActionViewList = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion,
        user,
        CaseProcessingActionGroup.CONSENT_PREPARATION
    );

    var modelAndView = new ModelAndView("fcs/application/consent/consentPreparation")
        .addObject("pageTitle", CONSENT_PREPARATION.getDisplayName())
        .addObject("applicationType", application.getType())
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(application.getId(), null, null)))
        .addObject("consentPreparationGroupActionViewList", consentPreparationGroupActionViewList);

    if (caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.MAIL_MERGE_ERROR_PRESENT)) {
      modelAndView.addObject(
          "singleErrorMessage",
          "Document mail merge errors are preventing this case from being assignable to a CAM"
      );
    }

    if (consentData != null) {
      var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

      var consentDataView = consentDataService.getConsentDataView(application, consentData, consentLengthType);
      var consentFigureUnitView = consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType);
      var consentPreparationConsentDataCardGroupActionViewList = caseProcessingActionService.getUserActionViewsForGroup(
          applicationVersion,
          user,
          CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DATA_CARD
      );

      var consentDocumentsSummaryCard = consentPreparationDocumentService.getConsentDocumentsSummaryCard(application);
      var consentPreparationConsentDocumentsCardGroupActionViewList = caseProcessingActionService.getUserActionViewsForGroup(
          applicationVersion,
          user,
          CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DOCUMENTS_CARD
      );

      modelAndView
          .addObject("consentLengthType", consentLengthType)
          .addObject("consentDataView", consentDataView)
          .addObject("consentFigureUnitView", consentFigureUnitView)
          .addObject("consentPreparationConsentDataCardGroupActionViewList", consentPreparationConsentDataCardGroupActionViewList)
          .addObject("consentDocumentsSummaryCard", consentDocumentsSummaryCard)
          .addObject(
              "consentPreparationConsentDocumentsCardGroupActionViewList",
              consentPreparationConsentDocumentsCardGroupActionViewList
          );

      if (applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
        var fieldEquityPartnersView = fieldEquityPartnerService.getFieldEquityPartnersView(applicationVersion);
        modelAndView
            .addObject("fieldEquityPartnersView", fieldEquityPartnersView)
            .addObject("regulatorIndustryAccessManagerRole", RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
            .addObject("industryAccessManagerRole", IndustryTeamRole.ACCESS_MANAGER)
            .addObject("consentRecipientRole", IndustryTeamRole.CONSENT_RECIPIENT);
      }
    }

    return modelAndView;
  }
}
