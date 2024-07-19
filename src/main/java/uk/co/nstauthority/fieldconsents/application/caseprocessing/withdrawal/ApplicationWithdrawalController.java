package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
public class ApplicationWithdrawalController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationWithdrawalService applicationWithdrawalService;

  private final WithdrawalRequestFormValidator withdrawalRequestFormValidator;

  private final WithdrawalResponseFormValidator withdrawalResponseFormValidator;

  private final WithdrawalRequestViewService withdrawalRequestViewService;

  public ApplicationWithdrawalController(ApplicationService applicationService,
                                         ApplicationVersionService applicationVersionService,
                                         ApplicationWithdrawalService applicationWithdrawalService,
                                         WithdrawalRequestFormValidator withdrawalRequestFormValidator,
                                         WithdrawalResponseFormValidator withdrawalResponseFormValidator,
                                         WithdrawalRequestViewService withdrawalRequestViewService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.withdrawalRequestFormValidator = withdrawalRequestFormValidator;
    this.withdrawalResponseFormValidator = withdrawalResponseFormValidator;
    this.withdrawalRequestViewService = withdrawalRequestViewService;
  }

  @GetMapping("withdrawal-request")
  @ActionEndPoint(OPERATOR_WITHDRAWAL_REQUEST)
  public ModelAndView getApplicationWithdrawalRequest(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    var withdrawalRequestForm = applicationWithdrawalService
        .getWithdrawalRequestForm(applicationVersion);

    var modelAndView = getApplicationWithdrawalRequestModelAndView(applicationVersion);
    modelAndView.addObject("form", withdrawalRequestForm);

    return modelAndView;
  }

  private ModelAndView getApplicationWithdrawalRequestModelAndView(ApplicationVersion applicationVersion) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    return new ModelAndView("fcs/application/withdrawalRequestForm")
        .addObject("submitUrl", ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .submitApplicationWithdrawalRequest(applicationId, null, null, null, null)))
        .addObject("backLinkUrl", ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(applicationId, null, null, null)))
        .addObject("applicationReference", applicationReference);
  }

  @PostMapping("withdrawal-request")
  @ActionEndPoint(OPERATOR_WITHDRAWAL_REQUEST)
  public ModelAndView submitApplicationWithdrawalRequest(@PathVariable Integer applicationId,
                                                         @ModelAttribute("form") WithdrawalRequestForm form,
                                                         BindingResult bindingResult,
                                                         ServiceUserDetail user,
                                                         RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(
        applicationId);

    withdrawalRequestFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getApplicationWithdrawalRequestModelAndView(applicationVersion);
    }

    applicationWithdrawalService.saveWithdrawalRequest(
        applicationVersion,
        form.getRequestText().getInputValue(),
        user
    );
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Withdrawal request sent");

    return ReverseRouter
        .redirect(on(IndustryCaseProcessingController.class).getIndustryCaseProcessing(applicationId, null, null, null));
  }

  @GetMapping("withdrawal-response")
  @ActionEndPoint(CASE_OFFICER_WITHDRAWAL_RESPONSE)
  public ModelAndView getApplicationWithdrawalResponse(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);
    var withdrawalResponseForm = applicationWithdrawalService
        .getWithdrawalResponseForm(applicationVersion);

    return getApplicationWithdrawalResponseModelAndView(applicationVersion)
        .addObject("form", withdrawalResponseForm);
  }

  private ModelAndView getApplicationWithdrawalResponseModelAndView(ApplicationVersion applicationVersion) {
    var withdrawalResponseStatuses = WithdrawalStatus.getWithdrawalResponseOptions();
    var applicationId = applicationVersion.getApplication().getId();

    return new ModelAndView("fcs/application/withdrawalResponseForm")
        .addObject("responseStatuses", withdrawalResponseStatuses)
        .addObject("withdrawalRequestView", withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .addObject("submitUrl", ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .submitApplicationWithdrawalResponse(applicationId, null, null, null, null)))
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null, null)));
  }

  @PostMapping("withdrawal-response")
  @ActionEndPoint(CASE_OFFICER_WITHDRAWAL_RESPONSE)
  public ModelAndView submitApplicationWithdrawalResponse(@PathVariable Integer applicationId,
                                                          @ModelAttribute("form") WithdrawalResponseForm form,
                                                          BindingResult bindingResult,
                                                          ServiceUserDetail user,
                                                          RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    withdrawalResponseFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getApplicationWithdrawalResponseModelAndView(applicationVersion);
    }

    applicationWithdrawalService.saveWithdrawalResponse(
        applicationVersion,
        form.getResponseStatus(),
        form.getResponseText().getInputValue(),
        user
    );
    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        String.format("Withdrawal %s",
            WithdrawalStatus.ACCEPTED.equals(form.getResponseStatus())
                ? "accepted"
                : "rejected")
    );

    return ReverseRouter
        .redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
