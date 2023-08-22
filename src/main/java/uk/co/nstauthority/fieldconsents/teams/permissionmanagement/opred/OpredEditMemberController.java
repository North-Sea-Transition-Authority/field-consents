package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.HasTeamPermission;
import uk.co.nstauthority.fieldconsents.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamMemberEditRolesValidatorHint;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamMemberRolesForm;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

@Controller
@RequestMapping("/permission-management/opred/{teamId}/edit/{wuaId}")
@HasTeamPermission(
    anyTeamPermissionOf = RolePermission.GRANT_ROLES,
    anyNonTeamPermissionOf = RolePermission.MANAGE_INDUSTRY_TEAMS
)
public class OpredEditMemberController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.OPRED;

  private final TeamMemberService teamMemberService;
  private final TeamMemberViewService teamMemberViewService;
  private final TeamMemberRoleService teamMemberRoleService;
  private final ControllerHelperService controllerHelperService;
  private final OpredTeamMemberEditRolesValidator opredTeamMemberEditRolesValidator;

  OpredEditMemberController(
      TeamMemberService teamMemberService,
      TeamMemberViewService teamMemberViewService,
      TeamMemberRoleService teamMemberRoleService,
      ControllerHelperService controllerHelperService,
      OpredTeamMemberEditRolesValidator opredTeamMemberEditRolesValidator,
      TeamService teamService
  ) {
    super(teamService);
    this.teamMemberService = teamMemberService;
    this.teamMemberViewService = teamMemberViewService;
    this.teamMemberRoleService = teamMemberRoleService;
    this.controllerHelperService = controllerHelperService;
    this.opredTeamMemberEditRolesValidator = opredTeamMemberEditRolesValidator;
  }

  @GetMapping
  public ModelAndView renderEditMember(@PathVariable("teamId") TeamId teamId,
                                       @PathVariable("wuaId") WebUserAccountId wuaId) {

    var form = new TeamMemberRolesForm();
    var team = getTeam(teamId, TEAM_TYPE);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    var userView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    form.setRoles(TeamRoleUtil.getRoleNames(teamMember.roles()));

    return getEditModelAndView(teamId, userView, form);

  }

  @PostMapping
  public ModelAndView editMember(@PathVariable("teamId") TeamId teamId,
                                 @PathVariable("wuaId") WebUserAccountId wuaId,
                                 @ModelAttribute("form") TeamMemberRolesForm form,
                                 BindingResult bindingResult) {

    var team = getTeam(teamId, TEAM_TYPE);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    var userView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    opredTeamMemberEditRolesValidator.validate(form, bindingResult,
        new TeamMemberEditRolesValidatorHint(team, teamMember));

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        form,
        () -> getEditModelAndView(teamId, userView, form),
        () -> {
          teamMemberRoleService.updateUserTeamRoles(team, teamMember.wuaId(), form.getRoles());
          return ReverseRouter.redirect(on(
              OpredTeamManagementController.class).renderMemberList(teamId));
        });

  }

  private ModelAndView getEditModelAndView(TeamId teamId, TeamMemberView userView, TeamMemberRolesForm form) {
    return new ModelAndView("fcs/permissionmanagement/addTeamMemberRolesPage")
        .addObject("form", form)
        .addObject("pageTitle", userView.getDisplayName())
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(OpredTeamRole.class))
        .addObject("backLinkUrl",
          ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(teamId))
        );
  }
}
