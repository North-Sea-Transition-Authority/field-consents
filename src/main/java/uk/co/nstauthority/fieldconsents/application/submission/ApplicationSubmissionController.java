package uk.co.nstauthority.fieldconsents.application.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseForm;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseFormValidator;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentController;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanEditApplication;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanPayAndSubmitApplication;
import uk.co.nstauthority.fieldconsents.feedback.FeedbackController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
public class ApplicationSubmissionController {

  static final String SUBMITTED_PAGE_TITLE = "Application submitted";
  static final String PAID_AND_SUBMITTED_PAGE_TITLE = "Application paid and submitted";
  static final String UPDATE_SUBMITTED_PAGE_TITLE = "Update submitted";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSubmissionService applicationSubmissionService;
  private final FieldConsentsAccessService fieldConsentsAccessService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final ApplicationPaymentService applicationPaymentService;
  private final ApplicationUpdateResponseFormValidator applicationUpdateResponseFormValidator;
  private final LicenceExpiryService licenceExpiryService;

  ApplicationSubmissionController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationSubmissionService applicationSubmissionService,
      FieldConsentsAccessService fieldConsentsAccessService,
      ApplicationSummaryService applicationSummaryService,
      ApplicationUpdateService applicationUpdateService,
      ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
      ApplicationPaymentService applicationPaymentService,
      ApplicationUpdateResponseFormValidator applicationUpdateResponseFormValidator,
      LicenceExpiryService licenceExpiryService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSubmissionService = applicationSubmissionService;
    this.fieldConsentsAccessService = fieldConsentsAccessService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.applicationPaymentService = applicationPaymentService;
    this.applicationUpdateResponseFormValidator = applicationUpdateResponseFormValidator;
    this.licenceExpiryService = licenceExpiryService;
  }

  @GetMapping("/review-and-submit")
  @UserCanEditApplication
  public ModelAndView getReviewAndSubmit(@PathVariable Integer applicationId,
                                         ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationUpdateResponseForm = ApplicationUpdateResponseForm.empty();

    return getReviewAndSubmitModelAndView(applicationVersion, user, applicationUpdateResponseForm);
  }

  private ModelAndView getReviewAndSubmitModelAndView(ApplicationVersion applicationVersion,
                                                      ServiceUserDetail user,
                                                      ApplicationUpdateResponseForm form) {
    var applicationId = applicationVersion.getApplication().getId();

    var userHasPayAndSubmitPermission = fieldConsentsAccessService.userHasAnyIndustryRole(
        user, applicationVersion, RoleGroup.INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES
    );

    var submittable = applicationSubmissionService.isSubmittable(applicationVersion);

    var modelAndView = new ModelAndView("fcs/application/reviewAndSubmit")
        .addObject("pageTitle", "Check your answers before submitting")
        .addObject("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(applicationId, null, null, null)))
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null)))
        .addObject("isSubmittable", submittable)
        .addObject("userHasPayAndSubmitPermission", userHasPayAndSubmitPermission)
        .addObject("applicationReference",
            applicationService.getApplicationReference(applicationVersion));

    var applicationUpdateOpen = applicationUpdateService.openApplicationUpdateExists(applicationVersion);

    if (applicationUpdateOpen) {
      modelAndView
          .addObject("applicationUpdateRequestView",
              applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
          .addObject("form", form)
          .addObject("requestedChangesOnlyRadio", ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY)
          .addObject("otherChangesRadio", ApplicationUpdateResponseType.OTHER_CHANGES);
    }

    if (submittable && userHasPayAndSubmitPermission) {
      var paymentRequired = !applicationUpdateOpen && applicationPaymentService.getPaymentAmountPence(applicationVersion) > 0;

      modelAndView.addObject("paymentRequired", paymentRequired);
    }

    modelAndView.addObject("expiringLicences",
        licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion));

    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView, user);

    return modelAndView;
  }

  @PostMapping("/review-and-submit")
  @UserCanPayAndSubmitApplication
  ModelAndView submitApplication(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ApplicationUpdateResponseForm form,
      BindingResult bindingResult,
      ServiceUserDetail user
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (!applicationSubmissionService.isSubmittable(applicationVersion)) {
      throw new RuntimeException("The application with id %s cannot be submitted!".formatted(applicationId));
    }

    var isApplicationUpdate = applicationUpdateService.openApplicationUpdateExists(applicationVersion);

    if (isApplicationUpdate) {
      applicationUpdateResponseFormValidator.validate(form, bindingResult);

      if (bindingResult.hasErrors()) {
        return getReviewAndSubmitModelAndView(applicationVersion, user, form);
      }

      applicationUpdateService.saveApplicationUpdateResponseAndSubmitApplicationUpdate(
          applicationVersion,
          form.responseType(),
          form.otherChangesDescription().getInputValue(),
          user
      );

      return ReverseRouter.redirect(on(ApplicationSubmissionController.class)
          .getApplicationUpdateSubmitted(applicationId));
    }

    if (applicationPaymentService.getPaymentAmountPence(applicationVersion) > 0) {
      applicationService.prepareApplicationForPayment(applicationVersion);

      return ReverseRouter.redirect(on(ApplicationPaymentController.class).getStartPayment(applicationId, null));
    } else {
      applicationSubmissionService.submitApplication(applicationVersion, user);

      return ReverseRouter.redirect(on(ApplicationSubmissionController.class).getApplicationSubmitted(applicationId));
    }
  }


  @GetMapping("/submitted")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  @UserCanPayAndSubmitApplication
  public ModelAndView getApplicationSubmitted(@PathVariable Integer applicationId) {
    return getSubmissionConfirmationModelAndView(applicationId, SUBMITTED_PAGE_TITLE);
  }

  @GetMapping("/paid-and-submitted")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  @UserCanPayAndSubmitApplication
  public ModelAndView getApplicationPaidAndSubmitted(@PathVariable Integer applicationId) {
    return getSubmissionConfirmationModelAndView(applicationId, PAID_AND_SUBMITTED_PAGE_TITLE);
  }

  @GetMapping("/update-submitted")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  @UserCanPayAndSubmitApplication
  public ModelAndView getApplicationUpdateSubmitted(@PathVariable Integer applicationId) {
    return getSubmissionConfirmationModelAndView(applicationId, UPDATE_SUBMITTED_PAGE_TITLE);
  }

  private ModelAndView getSubmissionConfirmationModelAndView(Integer applicationId, String pageTitle) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return new ModelAndView("fcs/application/submissionConfirmation")
        .addObject("pageTitle", pageTitle)
        .addObject("applicationReference", applicationService.generateApplicationReference(applicationVersion))
        .addObject("feedbackUrl", ReverseRouter.route(on(FeedbackController.class)
            .getApplicationFeedback(applicationVersion.getApplication().getId(), null)));
  }
}
