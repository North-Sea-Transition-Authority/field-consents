package uk.co.nstauthority.fieldconsents.teams.management;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamService;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.access.InvokingUserHasStaticRole;
import uk.co.nstauthority.fieldconsents.teams.management.form.NewOrganisationTeamForm;
import uk.co.nstauthority.fieldconsents.teams.management.form.NewOrganisationTeamFormValidator;

@Controller
@RequestMapping("/team-management/organisation")
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.INDUSTRY_ACCESS_MANAGER)
public class ScopedTeamManagementController {

  private final TeamManagementService teamManagementService;
  private final OrganisationApi organisationApi;
  private final NewOrganisationTeamFormValidator newOrganisationTeamFormValidator;
  private final EnergyPortalServiceProviderTeamService energyPortalServiceProviderTeamService;
  private final boolean isServiceAccessRequestsEnabled;

  ScopedTeamManagementController(
      TeamManagementService teamManagementService,
      OrganisationApi organisationApi,
      NewOrganisationTeamFormValidator newOrganisationTeamFormValidator,
      EnergyPortalServiceProviderTeamService energyPortalServiceProviderTeamService,
      Environment environment
  ) {
    this.teamManagementService = teamManagementService;
    this.organisationApi = organisationApi;
    this.newOrganisationTeamFormValidator = newOrganisationTeamFormValidator;
    this.energyPortalServiceProviderTeamService = energyPortalServiceProviderTeamService;
    this.isServiceAccessRequestsEnabled = environment.matchesProfiles("use-service-access-request");
  }

  // Add one of these get/post handlers for every scoped team time you want users to be able to create themselves.
  // only this creation logic needs to be added, once team is created normal TeamManagementController can be used.
  @GetMapping("/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.INDUSTRY_ACCESS_MANAGER)
  public ModelAndView renderCreateNewOrgTeam(@ModelAttribute("form") NewOrganisationTeamForm form) {
    return getModelAndView();
  }

  @PostMapping("/new")
  public ModelAndView handleCreateNewOrgTeam(@ModelAttribute("form") NewOrganisationTeamForm form, BindingResult bindingResult) {
    if (!newOrganisationTeamFormValidator.isValid(form, bindingResult)) {
      return getModelAndView();
    }

    var projection = new OrganisationGroupProjectionRoot()
        .organisationGroupId()
        .name();

    var orgGroupId = Integer.parseInt(form.getOrgGroupId());
    var requestPurpose = new RequestPurpose("Find org group to create team");
    var organisationGroup = organisationApi.findOrganisationGroup(orgGroupId, projection, requestPurpose)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Org group with id %s not found".formatted(form.getOrgGroupId())
        ));

    var scopeRef = TeamScopeReference.from(organisationGroup);
    var team = teamManagementService.createScopedTeam(organisationGroup.getName(), TeamType.INDUSTRY, scopeRef);

    if (isServiceAccessRequestsEnabled) {
      var serviceProviderTeam = new ServiceProviderTeamDto(
          team.getId().toString(),
          team.getScopeId(),
          ScopeType.ORGANISATION_GROUP
      );
      energyPortalServiceProviderTeamService.publishTeam(serviceProviderTeam);
    }
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  @GetMapping("/search")
  @ResponseBody
  public RestSearchResult searchOrganisation(@RequestParam(value = "term", required = false) String searchTerm) {
    var projection = new OrganisationGroupsProjectionRoot()
        .organisationGroupId()
        .name();

    var requestPurpose =  new RequestPurpose("Find org group to create team");
    var selectorResults = organisationApi.searchOrganisationGroups(searchTerm, projection, requestPurpose)
        .stream()
        .sorted(Comparator.comparing(OrganisationGroup::getName, String.CASE_INSENSITIVE_ORDER))
        .map(RestSearchItem::from)
        .toList();

    return new RestSearchResult(selectorResults);
  }

  private ModelAndView getModelAndView() {
    return new ModelAndView("fcs/teamManagement/scoped/createOrganisationTeam")
        .addObject(
            "organisationSearchUrl",
            ReverseRouter.route(on(ScopedTeamManagementController.class).searchOrganisation(null))
        );
  }

}
