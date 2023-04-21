package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamManagementController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

@Controller
@RequestMapping("/permission-management")
@AccessibleByServiceUsers
public class TeamListController {

  public static final String INDUSTRY_NEW_TEAM_FORM_URL =
      ReverseRouter.route(on(IndustryTeamManagementController.class).renderNewIndustryTeamForm(null));

  private final UserDetailService userDetailService;
  private final TeamService teamService;
  private final TeamManagementService teamManagementService;
  private final PermissionService permissionService;

  @Autowired
  public TeamListController(UserDetailService userDetailService,
                            TeamService teamService,
                            TeamManagementService teamManagementService,
                            PermissionService permissionService) {
    this.userDetailService = userDetailService;
    this.teamService = teamService;
    this.teamManagementService = teamManagementService;
    this.permissionService = permissionService;
  }

  @GetMapping
  public ModelAndView resolveTeamListEntryRoute() {
    var user = userDetailService.getUserDetail();
    var teams = teamService.getUserAccessibleTeams(user);
    if (teams.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User [%s] is not in a team".formatted(user.wuaId()));
    } else if (teams.size() == 1 && !permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))) {
      return getSingleTeamRedirect(teams.get(0));
    } else {
      return ReverseRouter.redirect(on(TeamListController.class).renderTeamList());
    }
  }

  private ModelAndView getSingleTeamRedirect(Team team) {
    return switch (team.getTeamType()) {
      case REGULATOR ->
          ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(team.toTeamId()));
      case INDUSTRY ->
          ReverseRouter.redirect(on(IndustryTeamManagementController.class).renderMemberList(team.toTeamId()));
    };
  }

  @GetMapping("/teams")
  public ModelAndView renderTeamList() {
    var user = userDetailService.getUserDetail();
    var teams = teamService.getUserAccessibleTeams(userDetailService.getUserDetail());
    var teamViews = teamManagementService.teamsToTeamViews(teams);

    var modelAndView = new ModelAndView("fcs/permissionmanagement/teamSelectionPage")
        .addObject("pageTitle", "Teams")
        .addObject("allTeams", teamViews);

    if (permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))) {
      modelAndView.addObject("industryNewTeamFormUrl", INDUSTRY_NEW_TEAM_FORM_URL);
    }

    return modelAndView;
  }

}
