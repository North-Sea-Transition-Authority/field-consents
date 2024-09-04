package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("/applications/{applicationId}/consent-issuing")
public class ConsentIssuingController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationContextService applicationContextService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ConsentLengthService consentLengthService;
  private final ConsentDataService consentDataService;
  private final ConsentFigureUnitService consentFigureUnitService;
  private final ConsentPreparationDocumentService consentPreparationDocumentService;
  private final ConsentIssuingApprovalService consentIssuingApprovalService;
  private final ConsentIssuingService consentIssuingService;
  private final ConsentService consentService;
  private final CaseStatusFlagService caseStatusFlagService;

  ConsentIssuingController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationContextService applicationContextService,
      CaseProcessingActionService caseProcessingActionService,
      ConsentLengthService consentLengthService,
      ConsentDataService consentDataService,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentPreparationDocumentService consentPreparationDocumentService,
      ConsentIssuingApprovalService consentIssuingApprovalService,
      ConsentIssuingService consentIssuingService,
      ConsentService consentService,
      CaseStatusFlagService caseStatusFlagService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationContextService = applicationContextService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consentLengthService = consentLengthService;
    this.consentDataService = consentDataService;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentPreparationDocumentService = consentPreparationDocumentService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
    this.consentIssuingService = consentIssuingService;
    this.consentService = consentService;
    this.caseStatusFlagService = caseStatusFlagService;
  }

  @GetMapping
  @ActionEndPoint(CaseProcessingActionItem.CONSENT_ISSUING)
  public ModelAndView getConsentIssuing(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var consentIssuingApprovalSummaryView = consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)
        .orElse(null);

    var consentIssuingGroupActionViewList = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion,
        user,
        CaseProcessingActionGroup.CONSENT_ISSUING
    );

    var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
    var consentData = consentDataService.getConsentData(application);
    var consentDataView = consentDataService.getConsentDataView(application, consentData, consentLengthType);
    var consentFigureUnitView = consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType);

    var consentDocumentsSummaryCard = consentPreparationDocumentService.getConsentDocumentsSummaryCard(application);

    var modelAndView = new ModelAndView("fcs/application/consent/consentIssuing")
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null, null)))
        .addObject("consentIssuingApprovalSummaryView", consentIssuingApprovalSummaryView)
        .addObject("consentIssuingGroupActionViewList", consentIssuingGroupActionViewList)
        .addObject("applicationType", application.getType())
        .addObject("consentLengthType", consentLengthType)
        .addObject("consentDataView", consentDataView)
        .addObject("consentFigureUnitView", consentFigureUnitView)
        .addObject("consentDocumentsSummaryCard", consentDocumentsSummaryCard);

    if (caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.MAIL_MERGE_ERROR_PRESENT)) {
      modelAndView.addObject(
          "singleErrorMessage",
          "Document mail merge errors are preventing this consent from being issuable"
      );
    }

    return modelAndView;
  }

  @PostMapping("/approve-for-issuing")
  @ActionEndPoint(CaseProcessingActionItem.APPROVE_FOR_ISSUING)
  public ModelAndView approveForIssuing(
      @PathVariable Integer applicationId,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    consentIssuingApprovalService.approveApplicationForConsentIssuing(application, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Application marked as ready to grant and issue"
    );

    return ReverseRouter.redirect(on(ConsentIssuingController.class).getConsentIssuing(applicationId, null));
  }

  @GetMapping("/issue-consent")
  @ActionEndPoint(CaseProcessingActionItem.ISSUE_CONSENT)
  public ModelAndView getIssueConsent(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var modelAndView = new ModelAndView("fcs/application/consent/issueConsent")
        .addObject("pageTitle", applicationService.generateApplicationReference(applicationVersion))
        .addObject("applicationContext", applicationContextService.getApplicationContext(applicationVersion))
        .addObject("cancelUrl", ReverseRouter.route(on(ConsentIssuingController.class)
            .getConsentIssuing(applicationId, null)));

    if (application.isRevision()) {
      var previousConsent = consentService.getPreviousConsent(application);
      var previousConsentApplicationReference = consentService.generateConsentApplicationReference(previousConsent);

      modelAndView.addObject("previousConsentApplicationReference", previousConsentApplicationReference);
    }

    return modelAndView;
  }

  @PostMapping("/unmark-for-issuing")
  @ActionEndPoint(CaseProcessingActionItem.UNAPPROVE_FOR_ISSUING)
  public ModelAndView unapproveForIssuing(
      @PathVariable Integer applicationId,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);

    consentIssuingApprovalService.deleteConsentIssuingApproval(application);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Application unmarked as ready to grant and issue"
    );

    return ReverseRouter.redirect(on(ConsentIssuingController.class).getConsentIssuing(applicationId, null));
  }

  @PostMapping("/issue-consent")
  @ActionEndPoint(CaseProcessingActionItem.ISSUE_CONSENT)
  public ModelAndView issueConsent(
      @PathVariable Integer applicationId,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var consent = consentIssuingService.issueConsent(applicationVersion, user);
    consentIssuingService.sendConsentIssuedEmails(applicationVersion, user, consent);

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Consent issued for %s".formatted(applicationReference)
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
