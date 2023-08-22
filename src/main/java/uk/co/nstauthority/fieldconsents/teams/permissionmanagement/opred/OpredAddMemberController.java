package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.HasTeamPermission;
import uk.co.nstauthority.fieldconsents.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AddTeamMemberForm;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AddTeamMemberValidator;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/permission-management/opred/{teamId}")
@HasTeamPermission(
    anyTeamPermissionOf = RolePermission.GRANT_ROLES,
    anyNonTeamPermissionOf = RolePermission.MANAGE_INDUSTRY_TEAMS
)
class OpredAddMemberController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.OPRED;

  private final ControllerHelperService controllerHelperService;
  private final AddTeamMemberValidator addTeamMemberValidator;
  private final EnergyPortalUserService energyPortalUserService;
  private final EnergyPortalConfiguration energyPortalConfiguration;

  OpredAddMemberController(
      ControllerHelperService controllerHelperService,
      AddTeamMemberValidator addTeamMemberValidator,
      EnergyPortalUserService energyPortalUserService,
      EnergyPortalConfiguration energyPortalConfiguration,
      TeamService teamService
  ) {
    super(teamService);
    this.controllerHelperService = controllerHelperService;
    this.addTeamMemberValidator = addTeamMemberValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.energyPortalConfiguration = energyPortalConfiguration;
  }

  @GetMapping("/add-member")
  ModelAndView renderAddTeamMember(@PathVariable("teamId") TeamId teamId) {
    var team = getTeam(teamId, TEAM_TYPE);
    var form = new AddTeamMemberForm();
    return getAddTeamMemberModelAndView(form, team);
  }

  @PostMapping("/add-member")
  public ModelAndView addMemberToTeamSubmission(@PathVariable("teamId") TeamId teamId,
                                                @ModelAttribute("form") AddTeamMemberForm form,
                                                BindingResult bindingResult) {
    var team = getTeam(teamId, TEAM_TYPE);
    addTeamMemberValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        form,
        () -> getAddTeamMemberModelAndView(form, team),
        () -> {
          var userToAdd = energyPortalUserService.findUserByUsername(form.getUsername()).get(0);
          var wuaId = new WebUserAccountId(userToAdd.webUserAccountId());
          if (teamService.isMemberOfTeam(teamId, wuaId)) {
            return ReverseRouter.redirect(on(OpredEditMemberController.class).renderEditMember(teamId, wuaId));
          }
          return ReverseRouter.redirect(on(
              OpredAddRolesController.class).renderAddTeamMemberRoles(teamId, wuaId));
        }
    );
  }

  private ModelAndView getAddTeamMemberModelAndView(AddTeamMemberForm form, Team team) {
    return new ModelAndView("fcs/permissionmanagement/addTeamMemberPage")
        .addObject("htmlTitle", "Add user to %s".formatted(team.getDisplayName()))
        .addObject("registrationUrl", energyPortalConfiguration.registrationUrl())
        .addObject("form", form)
        .addObject(
            "submitUrl",
            ReverseRouter.route(on(OpredAddMemberController.class)
                .addMemberToTeamSubmission(team.toTeamId(), form, ReverseRouter.emptyBindingResult()))
        )
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(team.toTeamId()))
        );
  }
}
