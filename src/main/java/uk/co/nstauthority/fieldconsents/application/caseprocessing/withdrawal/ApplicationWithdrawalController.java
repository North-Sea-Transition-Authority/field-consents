package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}")
@HasPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ApplicationWithdrawalController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationWithdrawalService applicationWithdrawalService;

  private final WithdrawalRequestFormValidator withdrawalRequestFormValidator;

  public ApplicationWithdrawalController(ApplicationService applicationService,
                                         ApplicationVersionService applicationVersionService,
                                         ApplicationWithdrawalService applicationWithdrawalService,
                                         WithdrawalRequestFormValidator withdrawalRequestFormValidator) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.withdrawalRequestFormValidator = withdrawalRequestFormValidator;
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
            .getIndustryCaseProcessing(applicationId, null)))
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

    applicationWithdrawalService.saveWithdrawalRequest(applicationVersion, form, user);
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Withdrawal request sent");

    return ReverseRouter
        .redirect(on(IndustryCaseProcessingController.class).getIndustryCaseProcessing(applicationId, null));
  }
}
