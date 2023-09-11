package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP;

import java.util.function.UnaryOperator;
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
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;

@Controller
@RequestMapping("applications/{applicationId}/assign-technical-reviewer")
public class TechnicalReviewAssignmentController {

  static final UnaryOperator<String> TECHNICAL_REVIEW_ASSIGNMENT_SUCCESS_MESSAGE =
      "You have assigned this technical review to %s"::formatted;

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final TechnicalReviewService technicalReviewService;

  private final TechnicalReviewAssignmentService technicalReviewAssignmentService;

  private final TechnicalReviewAssignmentFormValidator technicalReviewAssignmentFormValidator;

  private final EnergyPortalUserService energyPortalUserService;

  private final TeamMemberViewService teamMemberViewService;

  @Autowired
  TechnicalReviewAssignmentController(ApplicationService applicationService,
                                      ApplicationVersionService applicationVersionService,
                                      TechnicalReviewService technicalReviewService,
                                      TechnicalReviewAssignmentService technicalReviewAssignmentService,
                                      TechnicalReviewAssignmentFormValidator technicalReviewAssignmentFormValidator,
                                      EnergyPortalUserService energyPortalUserService,
                                      TeamMemberViewService teamMemberViewService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.technicalReviewService = technicalReviewService;
    this.technicalReviewAssignmentService = technicalReviewAssignmentService;
    this.technicalReviewAssignmentFormValidator = technicalReviewAssignmentFormValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.teamMemberViewService = teamMemberViewService;
  }

  @GetMapping
  @ActionEndPoint(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP)
  public ModelAndView getTechnicalReviewAssignment(@PathVariable Integer applicationId,
                                                   ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getTechnicalReviewAssignmentModelAndView(applicationVersion, user)
        .addObject("form", new TechnicalReviewAssignmentForm());
  }

  private ModelAndView getTechnicalReviewAssignmentModelAndView(ApplicationVersion applicationVersion,
                                                                ServiceUserDetail user) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    var technicalReview = technicalReviewService.getOpenTechnicalReview(applicationVersion);
    var technicalReviewerAssignmentCandidatesMap = teamMemberViewService
        .getUsersMap(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(technicalReview, user));

    return new ModelAndView("fcs/application/review/technicalReviewAssignment")
        .addObject("applicationReference", applicationReference)
        .addObject("technicalReviewerAssignmentCandidates", technicalReviewerAssignmentCandidatesMap)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(applicationId, null)));
  }

  @PostMapping
  @ActionEndPoint(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP)
  public ModelAndView assignTechnicalReviewer(@PathVariable Integer applicationId,
                                              @ModelAttribute("form") TechnicalReviewAssignmentForm form,
                                              ServiceUserDetail user,
                                              BindingResult bindingResult,
                                              RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    technicalReviewAssignmentFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getTechnicalReviewAssignmentModelAndView(applicationVersion, user);
    }

    var technicalReviewerUser = ServiceUserDetail.from(energyPortalUserService.getByWuaId(form.getTechnicalReviewerWuaId()));
    var technicalReview = technicalReviewService.getOpenTechnicalReview(applicationVersion);
    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, technicalReviewerUser, user);

    var successMessage = TECHNICAL_REVIEW_ASSIGNMENT_SUCCESS_MESSAGE.apply(
        user.wuaId().equals(technicalReviewerUser.wuaId())
            ? "yourself"
            : technicalReviewerUser.displayName());

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, successMessage);

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).getApplicationCaseProcessing(applicationId, null));
  }
}
