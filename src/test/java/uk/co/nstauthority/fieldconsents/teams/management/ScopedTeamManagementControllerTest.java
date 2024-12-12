package uk.co.nstauthority.fieldconsents.teams.management;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.form.NewOrganisationTeamFormValidator;

@ContextConfiguration(classes = ScopedTeamManagementController.class)
class ScopedTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private OrganisationApi organisationApi;

  @MockBean
  private NewOrganisationTeamFormValidator newOrganisationTeamFormValidator;

  private static ServiceUserDetail invokingUser;

  @BeforeAll
  public static void setUp() {
    invokingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1L)
        .build();
  }

  @Test
  void renderCreateNewOrgTeam() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @Test
  void renderCreateNewOrgTeam_noAccess() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null)))
            .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleCreateNewOrgTeam() throws Exception {
    var orgGroup = new OrganisationGroup();
    orgGroup.setOrganisationGroupId(50);
    orgGroup.setName("Some Org");

    var newTeam = new Team(UUID.randomUUID());

    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    when(newOrganisationTeamFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(organisationApi.findOrganisationGroup(eq(50), any(), any()))
        .thenReturn(Optional.of(orgGroup));

    when(teamManagementService.createScopedTeam(eq(orgGroup.getName()), eq(TeamType.INDUSTRY), refEq(TeamScopeReference.from("50", TeamScopeReference.ORGANISATION_GROUP_ID))))
        .thenReturn(newTeam);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("orgGroupId", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(newTeam.getId(), null))));
  }

  @Test
  void handleCreateNewOrgTeam_invalidForm() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    when(newOrganisationTeamFormValidator.isValid(any(), any()))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("orgGroupId", ""))
        .andExpect(status().isOk()); // No redirect to next page

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @Test
  void handleCreateNewOrgTeam_noAccess() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
            .with(csrf())
            .with(user(invokingUser))
            .param("orgGroupId", ""))
        .andExpect(status().isForbidden()); // No redirect to next page

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @Test
  void searchOrganisation() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    var orgGroup1 = new OrganisationGroup();
    orgGroup1.setOrganisationGroupId(1);
    orgGroup1.setName("SHELL one");

    var orgGroup2 = new OrganisationGroup();
    orgGroup2.setOrganisationGroupId(2);
    orgGroup2.setName("SHELL two");

    when(organisationApi.searchOrganisationGroups(eq("shell"), any(), any()))
        .thenReturn(List.of(orgGroup2, orgGroup1));

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).searchOrganisation("shell")))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"SHELL one"}, {"id":"2","text":"SHELL two"}]}
         """));
  }

}
