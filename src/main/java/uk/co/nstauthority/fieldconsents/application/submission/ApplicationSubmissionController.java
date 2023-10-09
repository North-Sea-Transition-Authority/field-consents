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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseForm;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseFormValidator;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentController;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/review-and-submit")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
public class ApplicationSubmissionController {

  static final String UPDATE_SUBMITTED_TITLE = "Updated submitted";
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSubmissionService applicationSubmissionService;
  private final ApplicationAccessService applicationAccessService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final ApplicationUpdateResponseFormValidator applicationUpdateResponseFormValidator;

  ApplicationSubmissionController(ApplicationService applicationService,
                                         ApplicationVersionService applicationVersionService,
                                         ApplicationSubmissionService applicationSubmissionService,
                                         ApplicationAccessService applicationAccessService,
                                         ApplicationSummaryService applicationSummaryService,
                                         ApplicationUpdateService applicationUpdateService,
                                         ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                         ApplicationUpdateResponseFormValidator applicationUpdateResponseFormValidator) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSubmissionService = applicationSubmissionService;
    this.applicationAccessService = applicationAccessService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.applicationUpdateResponseFormValidator = applicationUpdateResponseFormValidator;
  }

  @GetMapping
  @HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
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

    var userHasSubmitPermission = applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS
    );

    var modelAndView = new ModelAndView("fcs/application/reviewAndSubmit")
        .addObject("pageTitle", "Check your answers before submitting")
        .addObject("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(applicationId, null, null, null)))
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId)))
        .addObject("isSubmittable", applicationSubmissionService.isSubmittable(applicationVersion))
        .addObject("userHasSubmitPermission", userHasSubmitPermission)
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

    modelAndView.addObject("submitButtonText", !applicationUpdateOpen ? "Pay and submit" : "Submit");

    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);

    return modelAndView;
  }

  @PostMapping
  @HasApplicationPermission(permissions = RolePermission.SUBMIT_FCS_APPLICATIONS)
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

      return new ModelAndView("fcs/application/submissionConfirmation")
          .addObject("pageTitle", UPDATE_SUBMITTED_TITLE)
          .addObject("applicationReference", applicationService.generateApplicationReference(applicationVersion))
          .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)));
    }

    applicationService.prepareApplicationForPayment(applicationVersion);

    return ReverseRouter.redirect(on(ApplicationPaymentController.class).getStartPayment(applicationId));
  }
}
