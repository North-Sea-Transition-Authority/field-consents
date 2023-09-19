package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;

@Controller
@RequestMapping("applications/{applicationId}")
public class TechnicalReviewController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final TechnicalReviewService technicalReviewService;

  private final TechnicalReviewAssignmentService technicalReviewAssignmentService;

  private final TechnicalReviewRequestFormValidator technicalReviewRequestFormValidator;

  private final TeamMemberViewService teamMemberViewService;

  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  public TechnicalReviewController(ApplicationService applicationService,
                                   ApplicationVersionService applicationVersionService,
                                   TechnicalReviewService technicalReviewService,
                                   TechnicalReviewAssignmentService technicalReviewAssignmentService,
                                   TechnicalReviewRequestFormValidator technicalReviewRequestFormValidator,
                                   TeamMemberViewService teamMemberViewService,
                                   EnergyPortalUserService energyPortalUserService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.technicalReviewService = technicalReviewService;
    this.technicalReviewAssignmentService = technicalReviewAssignmentService;
    this.technicalReviewRequestFormValidator = technicalReviewRequestFormValidator;
    this.teamMemberViewService = teamMemberViewService;
    this.energyPortalUserService = energyPortalUserService;
  }

  @GetMapping("technical-review-request")
  @ActionEndPoint(TECHNICAL_REVIEW_REQUEST)
  public ModelAndView getTechnicalReviewRequest(@PathVariable Integer applicationId,
                                                ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var technicalReviewRequestForm = technicalReviewService.getTechnicalReviewRequestForm(applicationVersion);

    var modelAndView = getTechnicalReviewRequestModelAndView(applicationVersion, user);
    modelAndView.addObject("form", technicalReviewRequestForm);

    return modelAndView;
  }

  private ModelAndView getTechnicalReviewRequestModelAndView(ApplicationVersion applicationVersion,
                                                             ServiceUserDetail user) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    var technicalReviewerAssignmentCandidatesMap = teamMemberViewService
        .getUsersMap(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(user));

    return new ModelAndView("fcs/application/review/technicalReviewRequest")
        .addObject("applicationReference", applicationReference)
        .addObject("technicalReviewerAssignmentCandidates", technicalReviewerAssignmentCandidatesMap)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(applicationId, null)));
  }

  @PostMapping("technical-review-request")
  @ActionEndPoint(TECHNICAL_REVIEW_REQUEST)
  public ModelAndView startTechnicalReview(@PathVariable Integer applicationId,
                                           @ModelAttribute("form") TechnicalReviewRequestForm form,
                                           BindingResult bindingResult,
                                           ServiceUserDetail user,
                                           RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    technicalReviewRequestFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getTechnicalReviewRequestModelAndView(applicationVersion, user);
    }

    var technicalReviewerUser =
        ServiceUserDetail.from(energyPortalUserService.getByWuaId(form.getTechnicalReviewerWuaId()));

    var deadlineInstant = DateUtils.datePickerWithTimeStringToInstant(
        form.getDeadlineDate(), form.getDeadlineHours(), form.getDeadlineMinutes());

    technicalReviewService.saveTechnicalReviewRequest(applicationVersion, deadlineInstant,
        form.getRequestText().getInputValue(), technicalReviewerUser, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Technical review sent to %s".formatted(technicalReviewerUser.displayName())
    );

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).getApplicationCaseProcessing(applicationId, null));
  }
}
