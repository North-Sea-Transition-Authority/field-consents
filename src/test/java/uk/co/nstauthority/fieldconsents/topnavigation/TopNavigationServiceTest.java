package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class TopNavigationServiceTest {

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private TopNavigationService topNavigationService;

  private ServiceUserDetail user;

  private final Team regulatorTeam = TeamTestUtil.newBuilder()
      .withTeamType(TeamType.REGULATOR)
      .build();

  private final Team consulteeTeam = TeamTestUtil.newBuilder()
      .withTeamType(TeamType.CONSULTEE)
      .build();

  private final Team industryTeam = TeamTestUtil.newBuilder()
      .withTeamType(TeamType.INDUSTRY)
      .build();

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getTopNavigationItems_userWithNoTeamRoles() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of());

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).containsExactly(
        TopNavigationItem.WORK_AREA,
        TopNavigationItem.SEARCH,
        TopNavigationItem.TEAM_MANAGEMENT,
        TopNavigationItem.ENERGY_PORTAL
    );
  }

  @ParameterizedTest
  @EnumSource(
      value = Role.class,
      names = {"CASE_OFFICER", "CASE_MANAGER", "TECHNICAL_REVIEWER", "CONSENTS_AND_AUTHORISATIONS_MANAGER", "VIEWER"},
      mode = Mode.INCLUDE
  )
  void getTopNavigationItems_userCanManageAssets_regulator(Role role) {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(regulatorTeam)
            .withRole(role)
            .build()
    ));

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).contains(TopNavigationItem.MANAGE_ASSETS);
  }

  @Test
  void getTopNavigationItems_userCanManageAssets_consultee() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(consulteeTeam)
            .withRole(Role.ALLOCATOR)
            .build()
    ));

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .isNotEmpty()
        .doesNotContain(TopNavigationItem.MANAGE_ASSETS);
  }

  @ParameterizedTest
  @EnumSource(
      value = Role.class,
      names = {"CREATOR", "EDITOR", "SUBMITTER", "FINANCE_ADMINISTRATOR", "VIEWER", "CONSENT_RECIPIENT"},
      mode = Mode.INCLUDE
  )
  void getTopNavigationItems_userCanManageAssets_industry(Role role) {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(industryTeam)
            .withRole(role)
            .build()
    ));

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).contains(TopNavigationItem.MANAGE_ASSETS);
  }

  @Test
  void getTopNavigationItems_userCanManageFeePeriods() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(regulatorTeam)
            .withRole(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
            .build()
    ));

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).contains(TopNavigationItem.FEE_PERIODS);
  }

  @Test
  void getTopNavigationItems_userCanManageDocumentTemplates() {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(regulatorTeam)
            .withRole(Role.DOCUMENT_TEMPLATE_MANAGER)
            .build()
    ));

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).contains(TopNavigationItem.DOCUMENT_TEMPLATES);
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = {"CASE_MANAGER", "CONSENTS_AND_AUTHORISATIONS_MANAGER"}, mode = Mode.INCLUDE)
  void getTopNavigationItems_userCanSeeBulkCaseActions(Role role) {
    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(regulatorTeam)
            .withRole(role)
            .build()
    ));

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).contains(TopNavigationItem.BULK_ACTIONS);
  }

  @Test
  void getTopNavigationItems_withoutUser() {
    var topNavigationItems = topNavigationService.getTopNavigationItems(null);
    assertThat(topNavigationItems)
        .containsExactly(
            TopNavigationItem.WORK_AREA,
            TopNavigationItem.SEARCH,
            TopNavigationItem.TEAM_MANAGEMENT,
            TopNavigationItem.ENERGY_PORTAL
        );
  }
}
