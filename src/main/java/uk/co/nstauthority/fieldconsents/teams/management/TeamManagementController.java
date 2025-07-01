package uk.co.nstauthority.fieldconsents.teams.management;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.access.InvokingUserCanManageTeam;
import uk.co.nstauthority.fieldconsents.teams.management.access.InvokingUserCanViewTeam;
import uk.co.nstauthority.fieldconsents.teams.management.form.AddMemberForm;
import uk.co.nstauthority.fieldconsents.teams.management.form.AddMemberFormValidator;
import uk.co.nstauthority.fieldconsents.teams.management.form.MemberRolesForm;
import uk.co.nstauthority.fieldconsents.teams.management.form.MemberRolesFormValidator;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamTypeView;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamView;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@RestController
@RequestMapping("/team-management")
public class TeamManagementController {

  private final TeamManagementService teamManagementService;
  private final TeamQueryService teamQueryService;
  private final MemberRolesFormValidator memberRolesFormValidator;
  private final AddMemberFormValidator addMemberFormValidator;
  private final EnergyPortalConfiguration energyPortalConfiguration;
  private final EnergyPortalUserService energyPortalUserService;

  TeamManagementController(
      TeamManagementService teamManagementService,
      TeamQueryService teamQueryService,
      MemberRolesFormValidator memberRolesFormValidator,
      AddMemberFormValidator addMemberFormValidator,
      EnergyPortalConfiguration energyPortalConfiguration,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.teamManagementService = teamManagementService;
    this.teamQueryService = teamQueryService;
    this.memberRolesFormValidator = memberRolesFormValidator;
    this.addMemberFormValidator = addMemberFormValidator;
    this.energyPortalConfiguration = energyPortalConfiguration;
    this.energyPortalUserService = energyPortalUserService;
  }

  @GetMapping
  public ModelAndView renderTeamTypeList(ServiceUserDetail user) {
    var teamTypes = new ArrayList<>(teamManagementService.getTeamTypesUserIsMemberOf(user));

    if (teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER)) {
      // regulator with priv can manage org teams
      teamTypes.add(TeamType.INDUSTRY);
    }

    if (teamTypes.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No manageable teams for wuaId %d".formatted(user.wuaId()));
    }

    if (teamTypes.size() == 1) {
      var teamSlug = teamTypes.getFirst().getUrlSlug();
      return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamsOfType(teamSlug, user));
    }

    var teamTypeViews = teamTypes.stream().map(TeamTypeView::from).sorted().toList();

    return new ModelAndView("fcs/teamManagement/teamTypes")
        .addObject("teamTypeViews", teamTypeViews);
  }

  @GetMapping("/{teamTypeSlug}")
  public ModelAndView renderTeamsOfType(@PathVariable String teamTypeSlug, ServiceUserDetail user) {
    var teamType = TeamType.fromUrlSlug(teamTypeSlug)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No team type for url slug %s".formatted(teamTypeSlug)
        ));

    // if it's a static team, redirect to the single instance
    if (!teamType.isScoped()) {
      var team = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(teamType, user)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              "No manageable team of type %s for wuaId %d".formatted(teamType, user.wuaId())
          ));

      return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
    }

    // otherwise show the list of instances
    var userCanManageAnyOrganisationTeam = teamQueryService.userHasStaticRole(
        user,
        TeamType.REGULATOR,
        Role.INDUSTRY_ACCESS_MANAGER
    );

    var teams = new ArrayList<>(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(teamType, user));

    if (teams.isEmpty() && !userCanManageAnyOrganisationTeam) {
      // If user can create orgs, don't error as they need to be able to create new teams.
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "No manageable teams of type %s for wuaId %d".formatted(teamType, user.wuaId())
      );
    }

    if (teams.size() == 1 && !userCanManageAnyOrganisationTeam) {
      return ReverseRouter.redirect(on(TeamManagementController.class)
          .renderTeamMemberList(teams.getFirst().getId(), null));
    }

    var teamsViews = teams.stream().map(TeamView::from).sorted().toList();

    var modelAndView = new ModelAndView("fcs/teamManagement/teamInstances")
        .addObject("teamViews", teamsViews);

    if (userCanManageAnyOrganisationTeam) {
      modelAndView.addObject("createNewInstanceUrl", teamType.getCreateNewInstanceRoute().get());
    }

    return modelAndView;
  }

  @GetMapping("/team/{teamId}")
  @InvokingUserCanViewTeam
  public ModelAndView renderTeamMemberList(@PathVariable UUID teamId, ServiceUserDetail user) {
    var team = getTeamOrThrow(teamId);
    var teamMemberViews = teamManagementService.getTeamMemberViewsForTeam(team);

    return new ModelAndView("fcs/teamManagement/teamMembers")
        .addObject("teamName", team.getName())
        .addObject("teamMemberViews", teamMemberViews)
        .addObject("rolesInTeam", team.getTeamType().getAllowedRoles())
        .addObject("canManageTeam", teamManagementService.canManageTeam(team, user))
        .addObject(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(team.getId(), null))
        );
  }

  @GetMapping("/team/{teamId}/add-member")
  @InvokingUserCanManageTeam
  public ModelAndView renderAddMemberToTeam(@PathVariable UUID teamId, @ModelAttribute("form") AddMemberForm form) {
    getTeamOrThrow(teamId);
    return addMemberModelAndView(teamId);
  }

  private ModelAndView addMemberModelAndView(UUID teamId) {
    return new ModelAndView("fcs/teamManagement/addMember")
        .addObject("cancelUrl", teamMemberListUrl(teamId))
        .addObject("registerUrl", energyPortalConfiguration.registrationUrl())
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(teamId, null))
        );
  }

  @PostMapping("/team/{teamId}/add-member")
  @InvokingUserCanManageTeam
  public ModelAndView handleAddMemberToTeam(
      @PathVariable UUID teamId,
      @ModelAttribute("form") AddMemberForm form,
      BindingResult bindingResult
  ) {
    if (!addMemberFormValidator.isValid(form, bindingResult)) {
      return addMemberModelAndView(teamId);
    }

    var wuaId = energyPortalUserService.getEnergyPortalUsersThatCanLogin(form.getUsername())
        .stream()
        .filter(user -> !user.getIsAccountShared() && user.getCanLogin())
        .map(user -> user.getWebUserAccountId().longValue())
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "user with username %s not found or is shared account".formatted(form.getUsername()))
        );

    return ReverseRouter.redirect(on(TeamManagementController.class).renderUserTeamRoles(teamId, wuaId, null));
  }

  @GetMapping("/team/{teamId}/member/{wuaId}/")
  @InvokingUserCanManageTeam
  public ModelAndView renderUserTeamRoles(
      @PathVariable UUID teamId,
      @PathVariable Long wuaId,
      @ModelAttribute("form") MemberRolesForm form
  ) {
    var team = getTeamOrThrow(teamId);
    var teamMemberView = teamManagementService.getTeamMemberView(team, wuaId);

    form.setRoles(teamMemberView.roles().stream().map(Role::name).toList());

    return getUserTeamRolesModelAndView(team, wuaId);
  }

  @PostMapping("/team/{teamId}/member/{wuaId}/")
  @InvokingUserCanManageTeam
  public ModelAndView updateUserTeamRoles(
      @PathVariable UUID teamId,
      @PathVariable Long wuaId,
      @ModelAttribute("form") MemberRolesForm form,
      BindingResult bindingResult
  ) {
    var team = getTeamOrThrow(teamId);

    if (!memberRolesFormValidator.isValid(form, wuaId, team, bindingResult)) {
      return getUserTeamRolesModelAndView(team, wuaId);
    }

    var roles = form.getRoles().stream()
        .map(Role::valueOf)
        .toList();

    teamManagementService.setUserTeamRoles(wuaId, team, roles);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  @GetMapping("/team/{teamId}/member/{wuaId}/remove")
  @InvokingUserCanManageTeam
  public ModelAndView renderRemoveTeamMember(@PathVariable UUID teamId, @PathVariable Long wuaId) {
    var team = getTeamOrThrow(teamId);
    var teamMemberView = teamManagementService.getTeamMemberView(team, wuaId);
    var canRemoveTeamMember = teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId);

    return new ModelAndView("fcs/teamManagement/removeMember")
        .addObject("teamName", team.getName())
        .addObject("teamMemberView", teamMemberView)
        .addObject("canRemoveTeamMember", canRemoveTeamMember)
        .addObject("cancelUrl", teamMemberListUrl(teamId))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(teamId, null))
        );
  }

  @PostMapping("/team/{teamId}/member/{wuaId}/remove")
  @InvokingUserCanManageTeam
  public ModelAndView handleRemoveTeamMember(@PathVariable UUID teamId, @PathVariable Long wuaId) {
    var team = getTeamOrThrow(teamId);
    teamManagementService.removeUserFromTeam(wuaId, team);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  private ModelAndView getUserTeamRolesModelAndView(Team team, Long wuaId) {
    var teamMemberView = teamManagementService.getTeamMemberView(team, wuaId);
    var availableRoles = team.getTeamType().getAllowedRoles();
    var roleDisplayNameByEnumName = availableRoles.stream()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, Role::getDisplayName));

    return new ModelAndView("fcs/teamManagement/editMemberRoles")
        .addObject("teamMemberView", teamMemberView)
        .addObject("rolesNamesMap", roleDisplayNameByEnumName)
        .addObject("rolesInTeam", availableRoles)
        .addObject("cancelUrl", teamMemberListUrl(team.getId()))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null))
        );
  }

  private String teamMemberListUrl(UUID teamId) {
    return ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(teamId, null));
  }

  private Team getTeamOrThrow(UUID teamId) {
    return teamManagementService.getTeam(teamId).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Team with id %s not found".formatted(teamId)
    ));
  }
}