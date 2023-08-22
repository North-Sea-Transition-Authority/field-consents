package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamMemberRolesForm;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

@Controller
@RequestMapping("/permission-management/opred/{teamId}")
@HasTeamPermission(
    anyTeamPermissionOf = RolePermission.GRANT_ROLES,
    anyNonTeamPermissionOf = RolePermission.MANAGE_INDUSTRY_TEAMS
)
class OpredAddRolesController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.OPRED;

  private final ControllerHelperService controllerHelperService;
  private final EnergyPortalUserService energyPortalUserService;
  private final OpredTeamMemberRolesValidator opredTeamMemberRolesValidator;
  private final OpredTeamService opredTeamService;

  OpredAddRolesController(
      ControllerHelperService controllerHelperService,
      EnergyPortalUserService energyPortalUserService,
      OpredTeamMemberRolesValidator opredTeamMemberRolesValidator,
      OpredTeamService opredTeamService,
      TeamService teamService
  ) {
    super(teamService);
    this.controllerHelperService = controllerHelperService;
    this.energyPortalUserService = energyPortalUserService;
    this.opredTeamMemberRolesValidator = opredTeamMemberRolesValidator;
    this.opredTeamService = opredTeamService;
  }

  @GetMapping("/add-member/{wuaId}/roles")
  public ModelAndView renderAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                               @PathVariable("wuaId") WebUserAccountId webUserAccountId) {
    getTeam(teamId, TEAM_TYPE);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);
    return getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, new TeamMemberRolesForm())
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(OpredTeamRole.class))
        .addObject("backLinkUrl",
            ReverseRouter.route(on(OpredAddMemberController.class).renderAddTeamMember(teamId)));
  }

  @PostMapping("/add-member/{wuaId}/roles")
  protected ModelAndView saveAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                                @PathVariable("wuaId") WebUserAccountId webUserAccountId,
                                                @ModelAttribute("form") TeamMemberRolesForm form,
                                                BindingResult bindingResult) {
    var team = getTeam(teamId, TEAM_TYPE);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);
    opredTeamMemberRolesValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        form,
        () -> getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, form),
        () -> {
          var regulatorRoles = getRolesToAdd(form.getRoles());
          opredTeamService.addUserTeamRoles(team, energyPortalUser, regulatorRoles);

          return ReverseRouter.redirect(on(OpredTeamManagementController.class).renderMemberList(teamId));
        }
    );
  }

  private Set<OpredTeamRole> getRolesToAdd(Set<String> rolesToAdd) {
    return rolesToAdd.stream()
        .map(OpredTeamRole::getRoleFromString)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
  }

  private ModelAndView getAddTeamMemberRolesModelAndView(TeamId teamId,
                                                         EnergyPortalUserDto energyPortalUser,
                                                         TeamMemberRolesForm form) {
    return new ModelAndView("fcs/permissionmanagement/addTeamMemberRolesPage")
        .addObject("pageTitle", "What actions does %s perform?".formatted(energyPortalUser.displayName()))
        .addObject("form", form)
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(OpredTeamRole.class))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(OpredAddMemberController.class).renderAddTeamMember(teamId))
        );
  }

  // TODO - Merge with RegulatorAddRolesController usage
  private EnergyPortalUserDto getEnergyPortalUser(WebUserAccountId webUserAccountId) {
    var energyPortalUser = energyPortalUserService.findByWuaId(webUserAccountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No Energy Portal user with WUA_ID %s could be found".formatted(webUserAccountId)
        ));

    if (energyPortalUser.isSharedAccount()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Energy Portal user with WUA_ID %s is a shared account and is not allowed to be added to this service"
              .formatted(webUserAccountId)
      );
    }

    if (!energyPortalUser.canLogin()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          """
                  Energy Portal user with WUA_ID %s does not have login access to the Energy Portal and is
                  not allowed to be added to this service
              """
              .formatted(webUserAccountId)
      );
    }

    return energyPortalUser;
  }
}
