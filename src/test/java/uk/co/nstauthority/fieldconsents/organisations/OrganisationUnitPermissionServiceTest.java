package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1With2GroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2WithGroupsJsonNoGroups;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitPermissionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private TeamQueryService teamQueryService;

  @Spy
  @InjectMocks
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  private final ApplicationVersion applicationVersion = new ApplicationVersion();

  @BeforeEach
  void setUp() {
    applicationVersion.setPrimaryOperatorOuId(PRIMARY_OPERATOR_OU_ID_1);
  }

  @Test
  void getUserRolesForOperator_whenNoOrganisationGroups_thenEmpty() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit2WithGroupsJsonNoGroups);

    assertThat(organisationUnitPermissionService.getUserRolesForOperator(USER, applicationVersion))
        .isEmpty();
  }

  @Test
  void getUserRolesForOperator_whenNoRoleForAnyTeam_thenEmpty() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any())).thenReturn(orgUnit1With2GroupsJson);

    var teamScopeIds = orgUnit1With2GroupsJson.organisationGroups()
        .stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    when(
        teamQueryService.getTeamRoles(
            TeamType.INDUSTRY,
            TeamScopeReference.ORGANISATION_GROUP_ID,
            teamScopeIds
        )
    ).thenReturn(List.of());

    assertThat(organisationUnitPermissionService.getUserRolesForOperator(USER, applicationVersion)).isEmpty();
  }

  @Test
  void getUserRolesForOperator() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any())).thenReturn(orgUnit1With2GroupsJson);

    var team1 = TeamTestUtil.newBuilder()
        .withScopeId(ORG_GROUP_ID_1.toString())
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var team2 = TeamTestUtil.newBuilder()
        .withScopeId(ORG_GROUP_ID_2.toString())
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(
        teamQueryService.getTeamRoles(
          TeamType.INDUSTRY,
          TeamScopeReference.ORGANISATION_GROUP_ID,
          Set.of(String.valueOf(team1.getScopeId()), team2.getScopeId())
        )
    ).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(team1)
            .withWuaId(USER.wuaId())
            .withRole(Role.EDITOR)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(team2)
            .withWuaId(USER.wuaId() + 1) // this role is for another user, don't include it
            .withRole(Role.SUBMITTER)
            .build()
    ));

    assertThat(organisationUnitPermissionService.getUserRolesForOperator(USER, applicationVersion))
        .isEqualTo(Set.of(Role.EDITOR));
  }
}
