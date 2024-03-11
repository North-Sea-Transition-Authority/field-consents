package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-issuing")
public class ConsentIssuingController {

  private final ApplicationVersionService applicationVersionService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ConsentPreparationDocumentService consentPreparationDocumentService;
  private final ConsentIssuingApprovalService consentIssuingApprovalService;

  ConsentIssuingController(
      ApplicationVersionService applicationVersionService,
      CaseProcessingActionService caseProcessingActionService,
      ConsentPreparationDocumentService consentPreparationDocumentService,
      ConsentIssuingApprovalService consentIssuingApprovalService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consentPreparationDocumentService = consentPreparationDocumentService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
  }

  @GetMapping
  @ActionEndPoint(CaseProcessingActionItem.CONSENT_ISSUING)
  public ModelAndView getConsentIssuing(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var actionList = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion,
        user,
        CaseProcessingActionGroup.CONSENT_ISSUING
    );

    var consentDocumentsSummaryCard = consentPreparationDocumentService.getConsentDocumentsSummaryCard(application);

    var consentIssuingApprovalSummaryView = consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)
        .orElse(null);

    return new ModelAndView("fcs/application/consent/consentIssuing")
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)))
        .addObject("actionList", actionList)
        .addObject("consentDocumentsSummaryCard", consentDocumentsSummaryCard)
        .addObject("consentIssuingApprovalSummaryView", consentIssuingApprovalSummaryView);
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
}
