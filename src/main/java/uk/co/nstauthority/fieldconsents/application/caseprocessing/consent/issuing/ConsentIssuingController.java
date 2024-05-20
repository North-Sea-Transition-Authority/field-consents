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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
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
  private final ConsentPreparationDocumentService consentPreparationDocumentService;
  private final ConsentIssuingApprovalService consentIssuingApprovalService;
  private final ConsentIssuingService consentIssuingService;
  private final CaseStatusFlagService caseStatusFlagService;

  ConsentIssuingController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationContextService applicationContextService,
      CaseProcessingActionService caseProcessingActionService,
      ConsentPreparationDocumentService consentPreparationDocumentService,
      ConsentIssuingApprovalService consentIssuingApprovalService,
      ConsentIssuingService consentIssuingService,
      CaseStatusFlagService caseStatusFlagService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationContextService = applicationContextService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consentPreparationDocumentService = consentPreparationDocumentService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
    this.consentIssuingService = consentIssuingService;
    this.caseStatusFlagService = caseStatusFlagService;
  }

  @GetMapping
  @ActionEndPoint(CaseProcessingActionItem.CONSENT_ISSUING)
  public ModelAndView getConsentIssuing(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var consentIssuingGroupActionViewList = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion,
        user,
        CaseProcessingActionGroup.CONSENT_ISSUING
    );

    var consentPreparationConsentDocumentsCardGroupActionViewList = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion,
        user,
        CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DOCUMENTS_CARD
    );

    var consentDocumentsSummaryCard = consentPreparationDocumentService.getConsentDocumentsSummaryCard(application);

    var consentIssuingApprovalSummaryView = consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)
        .orElse(null);

    var modelAndView = new ModelAndView("fcs/application/consent/consentIssuing")
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)))
        .addObject("consentIssuingGroupActionViewList", consentIssuingGroupActionViewList)
        .addObject(
            "consentPreparationConsentDocumentsCardGroupActionViewList",
            consentPreparationConsentDocumentsCardGroupActionViewList
        )
        .addObject("consentDocumentsSummaryCard", consentDocumentsSummaryCard)
        .addObject("consentIssuingApprovalSummaryView", consentIssuingApprovalSummaryView);

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

    return new ModelAndView("fcs/application/consent/issueConsent")
        .addObject("pageTitle", applicationService.generateApplicationReference(applicationVersion))
        .addObject("applicationContext", applicationContextService.getApplicationContext(applicationVersion))
        .addObject("cancelUrl", ReverseRouter.route(on(ConsentIssuingController.class)
            .getConsentIssuing(applicationId, null)));
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

    consentIssuingService.issueConsent(applicationVersion, user);

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Consent issued for application %s".formatted(applicationReference)
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
