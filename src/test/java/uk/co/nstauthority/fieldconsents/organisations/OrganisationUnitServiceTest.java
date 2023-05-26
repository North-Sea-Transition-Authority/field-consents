package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnits;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.CREATE_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.SUBMIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaService.ALL_ORG_UNITS_WORK_AREA_PURPOSE;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitServiceTest {

  private static final String ORG_UNITS_SERVICE_PURPOSE = "Org unit service test purpose";

  private static final Set<RolePermission> CREATOR_PERMISSION_SET = Set.of(CREATE_FCS_APPLICATIONS);
  private static final Set<RolePermission> SUBMIT_PERMISSION_SET = Set.of(SUBMIT_FCS_APPLICATIONS);

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  OrganisationApi organisationApi;

  @Mock
  private TeamService teamService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @InjectMocks
  private OrganisationUnitService organisationUnitService;

  private Team regulatorTeam;

  private Team industryTeam;

  @BeforeEach
  void setup() {
    regulatorTeam = TeamTestUtil.Builder().withOrganisationGroupId(null).build();
    industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withOrganisationGroupId(ORG_GROUP_ID_1)
        .build();
  }

  @Test
  void searchOrganisationUnitsForUser_allTestOus() {
    when(organisationApi.searchOrganisationUnits(eq("oU"), any(), any()))
        .thenReturn(orgUnits);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(List.of(regulatorTeam));

    List<OrganisationUnitJson> allTestOus = organisationUnitService.searchOrganisationUnitsForUser("oU",
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(allTestOus).containsExactly(orgUnit1Json, orgUnit2Json, orgUnit3Json);
  }

  @Test
  void searchOrganisationUnitsForUser_singleTestOu() {
    when(organisationApi.searchOrganisationUnits(eq("2"), any(), any()))
        .thenReturn(List.of(orgUnit2));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(List.of(regulatorTeam));

    List<OrganisationUnitJson> singleTestOu = organisationUnitService.searchOrganisationUnitsForUser("2",
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(singleTestOu).containsExactly(orgUnit2Json);
  }

  @Test
  void searchOrganisationUnitsForUser_industryUser_permissionForOneOrgUnit() {
    var searchTerm = "oU";
    when(organisationApi.searchOrganisationUnits(eq(searchTerm), any(), any()))
        .thenReturn(orgUnits);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(industryTeam));
    when(permissionService.hasPermissionForTeam(industryTeam, USER, CREATOR_PERMISSION_SET))
        .thenReturn(true);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(ORG_GROUP_ID_1)))
        .thenReturn(List.of(orgUnit1Json));

    var foundOus = organisationUnitService.searchOrganisationUnitsForUser(searchTerm,
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(foundOus).containsExactly(orgUnit1Json);
  }

  @Test
  void searchOrganisationUnitsForUser_industryUser_permissionForNoOrgUnits() {
    var searchTerm = "oU";
    when(organisationApi.searchOrganisationUnits(eq(searchTerm), any(), any()))
        .thenReturn(orgUnits);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(industryTeam));
    when(permissionService.hasPermissionForTeam(industryTeam, USER, CREATOR_PERMISSION_SET))
        .thenReturn(false);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(Collections.emptyList()))
        .thenReturn(Collections.emptyList());

    var foundOus = organisationUnitService.searchOrganisationUnitsForUser(searchTerm,
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(foundOus).isEmpty();
  }

  @Test
  void findOrganisationUnitById_exists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.of(orgUnit1));

    var orgUnitJsonOptional = organisationUnitService
        .findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORG_UNITS_SERVICE_PURPOSE);
    assertThat(orgUnitJsonOptional).contains(orgUnit1Json);
  }

  @Test
  void findOrganisationUnitById_notExists() {
    when(organisationApi.findOrganisationUnit(eq(0), any(), any()))
        .thenReturn(Optional.empty());

    var orgUnitJsonOptional = organisationUnitService
        .findOrganisationUnitById(0, ORG_UNITS_SERVICE_PURPOSE);
    assertThat(orgUnitJsonOptional).isEmpty();
  }

  @Test
  void getOrganisationUnitById_exists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.of(orgUnit1));

    var orgUnitJson = organisationUnitService
        .getOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORG_UNITS_SERVICE_PURPOSE);
    assertThat(orgUnitJson).isEqualTo(orgUnit1Json);
  }

  @Test
  void getOrganisationUnitById_notExists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.empty());

    var ouId = orgUnit1.getOrganisationUnitId();
    assertThatThrownBy(() -> organisationUnitService
        .getOrganisationUnitById(ouId, ORG_UNITS_SERVICE_PURPOSE))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Organisation unit not found for id %s".formatted(ouId));
  }

  @Test
  void getOrganisationUnitByIdOrFallback_exists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.of(orgUnit1));

    var orgUnitJson = organisationUnitService.getOrganisationUnitByIdOrFallback(
        orgUnit1.getOrganisationUnitId(), ORG_UNITS_SERVICE_PURPOSE, orgUnit1.getName());
    assertThat(orgUnitJson).isEqualTo(orgUnit1Json);
  }

  @Test
  void getOrganisationUnitByIdOrFallback_notExistsUsesFallback() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.empty());

    var orgUnitJson = organisationUnitService.getOrganisationUnitByIdOrFallback(
        orgUnit1.getOrganisationUnitId(), ORG_UNITS_SERVICE_PURPOSE, orgUnit1.getName());
    assertThat(orgUnitJson).isEqualTo(orgUnit1Json);
  }

  @Test
  void findOrganisationUnitWithGroupsById_exists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.of(orgUnit1));

    var orgUnitJsonOptional = organisationUnitService
        .findOrganisationUnitWithGroupsById(orgUnit1.getOrganisationUnitId(), ORG_UNITS_SERVICE_PURPOSE);
    assertThat(orgUnitJsonOptional).isPresent();
    assertThat(orgUnitJsonOptional.get())
        .usingRecursiveComparison()
        .isEqualTo(orgUnit1WithGroupsJson);
  }

  @Test
  void findOrganisationUnitWithGroupsById_notExists() {
    when(organisationApi.findOrganisationUnit(eq(0), any(), any()))
        .thenReturn(Optional.empty());

    var orgUnitJsonOptional = organisationUnitService
        .findOrganisationUnitWithGroupsById(0, ORG_UNITS_SERVICE_PURPOSE);
    assertThat(orgUnitJsonOptional).isEmpty();
  }

  @Test
  void getOrganisationUnitWithGroupsById_exists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.of(orgUnit1));

    var orgUnitJson = organisationUnitService
        .getOrganisationUnitWithGroupsById(orgUnit1.getOrganisationUnitId(), ORG_UNITS_SERVICE_PURPOSE);
    assertThat(orgUnitJson)
        .usingRecursiveComparison()
        .isEqualTo(orgUnit1WithGroupsJson);
  }

  @Test
  void getOrganisationUnitWithGroupsById_notExists() {
    when(organisationApi.findOrganisationUnit(eq(orgUnit1.getOrganisationUnitId()), any(), any()))
        .thenReturn(Optional.empty());

    var ouId = orgUnit1.getOrganisationUnitId();
    assertThatThrownBy(() -> organisationUnitService
        .getOrganisationUnitWithGroupsById(ouId, ORG_UNITS_SERVICE_PURPOSE))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Organisation unit not found for id %s".formatted(ouId));
  }

  @Test
  void getOperatorsUserHasPermissionsFor_whenNoOperators() {
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(team1, team2));
    when(permissionService.hasPermissionForTeam(team1, USER, SUBMIT_PERMISSION_SET))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, SUBMIT_PERMISSION_SET))
        .thenReturn(false);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(Collections.emptyList()))
        .thenReturn(Collections.emptyList());

    assertThat(organisationUnitService.getOperatorsUserHasPermissionsFor(USER, SUBMIT_PERMISSION_SET))
        .isEmpty();
  }

  @Test
  void getOperatorsUserHasPermissionsFor_whenOneOperator() {
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(team1, team2));
    when(permissionService.hasPermissionForTeam(team1, USER, SUBMIT_PERMISSION_SET))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, SUBMIT_PERMISSION_SET))
        .thenReturn(true);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(team2.getOrganisationGroupId())))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(organisationUnitService.getOperatorsUserHasPermissionsFor(USER, SUBMIT_PERMISSION_SET))
        .isEqualTo(List.of(orgUnit2Json));
  }

  @Test
  void getOperatorsUserHasPermissionsFor_whenManyOperators() {
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(team1, team2));
    when(permissionService.hasPermissionForTeam(team1, USER, SUBMIT_PERMISSION_SET))
        .thenReturn(true);
    when(permissionService.hasPermissionForTeam(team2, USER, SUBMIT_PERMISSION_SET))
        .thenReturn(true);
    when(organisationGroupQueryService
        .getOrganisationUnitsByOrganisationGroupIds(List.of(team1.getOrganisationGroupId(), team2.getOrganisationGroupId())))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(organisationUnitService.getOperatorsUserHasPermissionsFor(USER, SUBMIT_PERMISSION_SET))
        .isEqualTo(List.of(orgUnit1Json, orgUnit2Json));
  }

  @Test
  void getOrganisationUnitsByIds_emptyList() {
    when(organisationApi.getOrganisationUnitsByIds(
        anyList(),
        any(OrganisationUnitsProjectionRoot.class),
        any(RequestPurpose.class))
    ).thenReturn(Collections.emptyList());

    assertThat(organisationUnitService.getOrganisationUnitsByIds(
        List.of(PRIMARY_OPERATOR_OU_ID_1, PRIMARY_OPERATOR_OU_ID_2),
        ALL_ORG_UNITS_WORK_AREA_PURPOSE)
    ).isEqualTo(Collections.emptyList());
  }

  @Test
  void getOrganisationUnitsByIds() {
    when(organisationApi.getOrganisationUnitsByIds(
        anyList(),
        any(OrganisationUnitsProjectionRoot.class),
        any(RequestPurpose.class))
    ).thenReturn(List.of(orgUnit1, orgUnit2));

    assertThat(organisationUnitService.getOrganisationUnitsByIds(
        List.of(PRIMARY_OPERATOR_OU_ID_1, PRIMARY_OPERATOR_OU_ID_2),
        ALL_ORG_UNITS_WORK_AREA_PURPOSE)
    ).isEqualTo(List.of(orgUnit1Json, orgUnit2Json));
  }
}
