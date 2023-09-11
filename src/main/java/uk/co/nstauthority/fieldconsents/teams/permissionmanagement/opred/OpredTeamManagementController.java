package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.IsMemberOfTeam;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;


@Controller
@RequestMapping("/permission-management/opred")
public class OpredTeamManagementController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.OPRED;

  private final TeamMemberViewService teamMemberViewService;
  private final UserDetailService userDetailService;
  private final PermissionService permissionService;

  OpredTeamManagementController(
      TeamMemberViewService teamMemberViewService,
      TeamService teamService,
      UserDetailService userDetailService,
      PermissionService permissionService
  ) {
    super(teamService);
    this.teamMemberViewService = teamMemberViewService;
    this.userDetailService = userDetailService;
    this.permissionService = permissionService;
  }

  @GetMapping("/{teamId}")
  @IsMemberOfTeam
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {
    var user = userDetailService.getUserDetail();

    var team = getTeam(teamId, TEAM_TYPE);
    var modelAndView = new ModelAndView("fcs/permissionmanagement/teamMembersPage")
        .addObject("pageTitle", "Manage %s".formatted(team.getDisplayName()))
        .addObject("teamName", team.getDisplayName())
        .addObject("teamRoles", OpredTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getTeamMemberViewsForTeam(team));

    if (teamService.canUserAccessMultipleTeams(user)) {
      modelAndView
          .addObject("backLinkUrl",
              ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()));
    }

    if (permissionService.hasPermissionForTeam(team, user, Set.of(RolePermission.GRANT_ROLES))) {
      modelAndView
          .addObject("addTeamMemberUrl",
              ReverseRouter.route(on(OpredAddMemberController.class).renderAddTeamMember(teamId)))
          .addObject("canRemoveUsers", true)
          .addObject("canEditUsers", true);
    }
    return modelAndView;
  }
}
