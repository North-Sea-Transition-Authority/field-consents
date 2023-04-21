package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.authorisation.IsMemberOfTeamOrHasRegulatorRole;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupRestController;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;


@Controller
@RequestMapping("/permission-management/industry")
@AccessibleByServiceUsers
public class IndustryTeamManagementController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.INDUSTRY;

  private final ControllerHelperService controllerHelperService;
  private final TeamMemberViewService teamMemberViewService;
  private final IndustryTeamService industryTeamService;
  private final UserDetailService userDetailService;
  private final IndustryNewTeamFormValidator industryNewTeamFormValidator;
  private final OrganisationGroupQueryService organisationGroupService;
  private final PermissionService permissionService;
  private final TeamService teamService;

  @Autowired
  IndustryTeamManagementController(ControllerHelperService controllerHelperService,
                                   TeamMemberViewService teamMemberViewService,
                                   TeamService teamService,
                                   IndustryTeamService industryTeamService,
                                   UserDetailService userDetailService,
                                   IndustryNewTeamFormValidator industryNewTeamFormValidator,
                                   OrganisationGroupQueryService organisationGroupService,
                                   PermissionService permissionService,
                                   TeamService teamService1) {
    super(teamService);
    this.controllerHelperService = controllerHelperService;
    this.teamMemberViewService = teamMemberViewService;
    this.industryTeamService = industryTeamService;
    this.userDetailService = userDetailService;
    this.industryNewTeamFormValidator = industryNewTeamFormValidator;
    this.organisationGroupService = organisationGroupService;
    this.permissionService = permissionService;
    this.teamService = teamService1;
  }

  @GetMapping("/new-team")
  @HasPermission(permissions = RolePermission.MANAGE_INDUSTRY_TEAMS)
  public ModelAndView renderNewIndustryTeamForm(@ModelAttribute("form") IndustryNewTeamForm form) {

    var organisationGroupSearchRestUrl =
        ReverseRouter.route(on(OrganisationGroupRestController.class).getOrganisationGroupSearchResults(null));
    return new ModelAndView("fcs/permissionmanagement/addTeam")
        .addObject("pageTitle", "Add new industry team")
        .addObject("organisationGroupSearchRestUrl", organisationGroupSearchRestUrl)
        .addObject("submitFormUrl", ReverseRouter.route(on(IndustryTeamManagementController.class)
            .addNewIndustryTeam(null, null, null)))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
        );
  }

  @PostMapping("/new-team")
  @HasPermission(permissions = RolePermission.MANAGE_INDUSTRY_TEAMS)
  public ModelAndView addNewIndustryTeam(@ModelAttribute("form") IndustryNewTeamForm form,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes) {

    industryNewTeamFormValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        form,
        () -> renderNewIndustryTeamForm(form),
        () -> {
          var groupId = Integer.parseInt(form.getOrganisationGroupId());
          var orgGroup =
              organisationGroupService.getOrganisationGroupById(groupId)
                  .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "No organisation group with id %d could not be found".formatted(groupId)
                  ));

          var groupName = orgGroup.getOrganisationGroupName();
          var team = industryTeamService.getTeamByOrganisationGroupId(groupId)
              .orElseGet(() -> {
                var t = industryTeamService.createTeam(
                    groupName,
                    groupId
                );

                NotificationBannerUtil.addSuccessNotification(redirectAttributes,
                    "Added new organisation group team",
                    "Team for organisation group %s has been created".formatted(groupName));

                return t;
              });

          return ReverseRouter.redirect(on(IndustryTeamManagementController.class)
              .renderMemberList(team.toTeamId()));
        });
  }

  @GetMapping("/{teamId}")
  @IsMemberOfTeamOrHasRegulatorRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {
    var user = userDetailService.getUserDetail();

    var team = getTeam(teamId, TEAM_TYPE);
    var modelAndView = new ModelAndView("fcs/permissionmanagement/teamMembersPage")
        .addObject("pageTitle", "Manage %s".formatted(team.getDisplayName()))
        .addObject("teamName", team.getDisplayName())
        .addObject("teamRoles", IndustryTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getTeamMemberViewsForTeam(team));

    if (teamService.canUserAccessMultipleTeams(user)) {
      modelAndView
          .addObject("backLinkUrl",
              ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()));
    }

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