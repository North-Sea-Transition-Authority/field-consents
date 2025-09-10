package uk.co.nstauthority.fieldconsents.teams.management;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamService;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.form.NewOrganisationTeamFormValidator;

@ActiveProfiles("use-service-access-request")
@ContextConfiguration(classes = ScopedTeamManagementController.class)
class ScopedTeamManagementControllerServiceAccessRequestsEnabledTest extends AbstractControllerTest {

  @MockitoBean
  private OrganisationApi organisationApi;

  @MockitoBean
  private NewOrganisationTeamFormValidator newOrganisationTeamFormValidator;

  @MockitoBean
  private EnergyPortalServiceProviderTeamService energyPortalServiceProviderTeamService;


  private static ServiceUserDetail invokingUser;

  @BeforeAll
  static void setUp() {
    invokingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1L)
        .build();
  }

  @Test
  void handleCreateNewOrgTeam_serviceAccessRequestsEnabled() throws Exception {
    var orgGroup = new OrganisationGroup();
    orgGroup.setOrganisationGroupId(50);
    orgGroup.setName("Some Org");

    var newTeam = TeamTestUtil.newBuilder().build();

    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    when(newOrganisationTeamFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(organisationApi.findOrganisationGroup(eq(50), any(), any()))
        .thenReturn(Optional.of(orgGroup));

    when(teamManagementService.createScopedTeam(eq(orgGroup.getName()), eq(TeamType.INDUSTRY), refEq(
        TeamScopeReference.from("50", TeamScopeReference.ORGANISATION_GROUP_ID))))
        .thenReturn(newTeam);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
            .with(csrf())
            .with(user(invokingUser))
            .param("orgGroupId", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(newTeam.getId(), null))));

    var expectedServiceProviderTeamDto = new ServiceProviderTeamDto(
        newTeam.getId().toString(),
        newTeam.getScopeId(),
        ScopeType.ORGANISATION_GROUP,
        newTeam.getTeamType().name()
    );

    verify(energyPortalServiceProviderTeamService)
        .publishTeam(expectedServiceProviderTeamDto);
  }
}