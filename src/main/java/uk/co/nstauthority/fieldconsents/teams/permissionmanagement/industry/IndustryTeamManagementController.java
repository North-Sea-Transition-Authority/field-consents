package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.IsMemberOfTeamOrHasRegulatorRole;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;


@Controller
@RequestMapping("/permission-management/industry")
@IsMemberOfTeamOrHasRegulatorRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
public class IndustryTeamManagementController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.INDUSTRY;

  private final TeamMemberViewService teamMemberViewService;
  private final UserDetailService userDetailService;
  private final PermissionService permissionService;

  @Autowired
  IndustryTeamManagementController(TeamMemberViewService teamMemberViewService,
                                   TeamService teamService,
                                   UserDetailService userDetailService,
                                   PermissionService permissionService) {
    super(teamService);
    this.teamMemberViewService = teamMemberViewService;
    this.userDetailService = userDetailService;
    this.permissionService = permissionService;
  }

  @GetMapping("/{teamId}")
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {
    var user = userDetailService.getUserDetail();

    var team = getTeam(teamId, TEAM_TYPE);
    var modelAndView = new ModelAndView("fcs/permissionmanagement/teamMembersPage")
        .addObject("pageTitle", "Manage %s".formatted(team.getDisplayName()))
        .addObject("teamName", team.getDisplayName())
        .addObject("teamRoles", IndustryTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getTeamMemberViewsForTeam(team));

    if (permissionService.hasPermissionForTeam(teamId, user, Set.of(RolePermission.GRANT_ROLES))
        || permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))) {
      modelAndView
          .addObject("addTeamMemberUrl",
              ReverseRouter.route(on(IndustryAddMemberController.class).renderAddTeamMember(teamId)))
          .addObject("canRemoveUsers", true)
          .addObject("canEditUsers", true);
    }
    return modelAndView;
  }
}