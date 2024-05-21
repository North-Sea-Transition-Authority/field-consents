package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_REASSIGN_OWNERSHIP;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
public class CamAssignmentController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final CamAssignmentService camAssignmentService;

  private final CamAssignmentFormValidator camAssignmentFormValidator;

  private final EnergyPortalUserService energyPortalUserService;

  private final TeamMemberViewService teamMemberViewService;

  public CamAssignmentController(ApplicationService applicationService,
                                 ApplicationVersionService applicationVersionService,
                                 CamAssignmentService camAssignmentService,
                                 CamAssignmentFormValidator camAssignmentFormValidator,
                                 EnergyPortalUserService energyPortalUserService,
                                 TeamMemberViewService teamMemberViewService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.camAssignmentService = camAssignmentService;
    this.camAssignmentFormValidator = camAssignmentFormValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.teamMemberViewService = teamMemberViewService;
  }

  @GetMapping("assign-to-cam")
  @ActionEndPoint({CAM_ASSIGN_OWNERSHIP, CAM_REASSIGN_OWNERSHIP})
  public ModelAndView getCamAssignment(@PathVariable Integer applicationId,
                                       ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getCamAssignmentModelAndView(applicationVersion, user)
        .addObject("form", new CamAssignmentForm());
  }

  private ModelAndView getCamAssignmentModelAndView(ApplicationVersion applicationVersion,
                                                    ServiceUserDetail user) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    var camUserAssignmentCandidatesMap = teamMemberViewService
        .getUsersMap(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user));

    return new ModelAndView("fcs/application/camAssignment")
        .addObject("applicationReference", applicationReference)
        .addObject("camUserAssignmentCandidates", camUserAssignmentCandidatesMap)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(applicationId, null)));
  }

  @PostMapping("assign-to-cam")
  @ActionEndPoint({CAM_ASSIGN_OWNERSHIP, CAM_REASSIGN_OWNERSHIP})
  public ModelAndView assignCamUser(@PathVariable Integer applicationId,
                                    @ModelAttribute("form") CamAssignmentForm form,
                                    ServiceUserDetail user,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    camAssignmentFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getCamAssignmentModelAndView(applicationVersion, user);
    }

    var camUser = ServiceUserDetail.from(energyPortalUserService.getByWuaId(form.getCamWuaId()));

    camAssignmentService.assignCamUser(applicationVersion, camUser, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "You have assigned %s to %s".formatted(applicationService.generateApplicationReference(applicationVersion),
            camUser.displayName())
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
