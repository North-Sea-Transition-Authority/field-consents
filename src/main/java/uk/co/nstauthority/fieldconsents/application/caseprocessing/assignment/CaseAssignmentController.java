package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.RETURN_TO_CASE_OFFICER;

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
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
public class CaseAssignmentController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final CaseAssignmentService caseAssignmentService;

  private final CaseAssignmentFormValidator caseAssignmentFormValidator;

  private final EnergyPortalUserService energyPortalUserService;

  private final TeamMemberViewService teamMemberViewService;

  @Autowired
  CaseAssignmentController(ApplicationService applicationService,
                           ApplicationVersionService applicationVersionService,
                           CaseAssignmentService caseAssignmentService,
                           CaseAssignmentFormValidator caseAssignmentFormValidator,
                           EnergyPortalUserService energyPortalUserService,
                           TeamMemberViewService teamMemberViewService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.caseAssignmentService = caseAssignmentService;
    this.caseAssignmentFormValidator = caseAssignmentFormValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.teamMemberViewService = teamMemberViewService;
  }

  @GetMapping("assign")
  @ActionEndPoint({CASE_OFFICER_ASSIGN_OWNERSHIP, CASE_OFFICER_REASSIGN_OWNERSHIP})
  public ModelAndView getCaseAssignment(@PathVariable Integer applicationId,
                                        ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getCaseAssignmentModelAndView(applicationVersion, user)
        .addObject("form", new CaseAssignmentForm());
  }

  private ModelAndView getCaseAssignmentModelAndView(ApplicationVersion applicationVersion,
                                                     ServiceUserDetail user) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    var caseOfficerAssignmentCandidatesMap = teamMemberViewService
        .getUsersMap(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, user));

    return new ModelAndView("fcs/application/caseAssignment")
        .addObject("applicationReference", applicationReference)
        .addObject("caseOfficerAssignmentCandidates", caseOfficerAssignmentCandidatesMap)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(applicationId, null, null)));
  }

  @PostMapping("assign")
  @ActionEndPoint({CASE_OFFICER_ASSIGN_OWNERSHIP, CASE_OFFICER_REASSIGN_OWNERSHIP})
  public ModelAndView assignCaseOfficer(@PathVariable Integer applicationId,
                                        @ModelAttribute("form") CaseAssignmentForm form,
                                        ServiceUserDetail user,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    caseAssignmentFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getCaseAssignmentModelAndView(applicationVersion, user);
    }

    var caseOfficerUser = ServiceUserDetail.from(energyPortalUserService.getByWuaId(form.getCaseOfficerWuaId()));

    caseAssignmentService.assignCaseOfficer(applicationVersion, caseOfficerUser, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "You have assigned this case to %s".formatted(caseOfficerUser.displayName())
    );

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null, null));
  }

  @PostMapping("take-ownership-case-officer")
  @ActionEndPoint(CASE_OFFICER_TAKE_OWNERSHIP)
  public ModelAndView takeOwnershipCaseOfficer(@PathVariable Integer applicationId,
                                               ServiceUserDetail user,
                                               RedirectAttributes redirectAttributes) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    caseAssignmentService.assignCaseOfficer(applicationVersion, user, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "You have taken ownership of this case"
    );

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null, null));
  }

  @PostMapping("release-ownership-case-officer")
  @ActionEndPoint(CASE_OFFICER_RELEASE_OWNERSHIP)
  public ModelAndView releaseOwnershipCaseOfficer(@PathVariable Integer applicationId,
                                                  RedirectAttributes redirectAttributes,
                                                  ServiceUserDetail user) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    caseAssignmentService.unassignCaseOfficer(applicationVersion, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "You have released ownership of this case"
    );

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null, null));
  }

  @PostMapping("return-to-case-officer")
  @ActionEndPoint(RETURN_TO_CASE_OFFICER)
  public ModelAndView returnToCaseOfficer(@PathVariable Integer applicationId,
                                          ServiceUserDetail user,
                                          RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var caseOfficer = ServiceUserDetail.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId())));

    caseAssignmentService.returnToCaseOfficer(applicationVersion, user);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "You have reassigned %s to %s".formatted(applicationService.generateApplicationReference(applicationVersion),
            caseOfficer.displayName())
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
