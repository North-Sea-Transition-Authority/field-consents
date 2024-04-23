package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnits;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.CREATE_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitSearchServiceTest {

  private static final String ORG_UNITS_SERVICE_PURPOSE = "Org unit service test purpose";

  private static final Set<RolePermission> CREATOR_PERMISSION_SET = Set.of(CREATE_FCS_APPLICATIONS);
  private static final Set<RolePermission> SUBMITTER_PERMISSION_SET = Set.of(PAY_AND_SUBMIT_FCS_APPLICATIONS);
  private static final Set<RolePermission> ALLOCATOR_PERMISSION_SET = Set.of(ALLOCATE_CONSULTATION);

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationApi organisationApi;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private TeamService teamService;

  @InjectMocks
  private OrganisationUnitSearchService organisationUnitSearchService;

  private Team regulatorTeam;
  private Team consulteeTeam;

  @BeforeEach
  void setup() {
    regulatorTeam = TeamTestUtil.Builder().withOrganisationGroupId(null).build();
    consulteeTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).withOrganisationGroupId(null).build();
  }

  @Test
  void searchOrganisationUnitsForUser_regulatorUser_allTestOus() {
    when(organisationApi.searchOrganisationUnits(eq("oU"), any(), any()))
        .thenReturn(orgUnits);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(List.of(regulatorTeam));

    List<OrganisationUnitJson> allTestOus = organisationUnitSearchService.searchOrganisationUnitsForUser("oU",
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(allTestOus).containsExactly(orgUnit1Json, orgUnit2Json, orgUnit3Json);
  }

  @Test
  void searchOrganisationUnitsForUser_regulatorUser_singleTestOu() {
    when(organisationApi.searchOrganisationUnits(eq("2"), any(), any()))
        .thenReturn(List.of(orgUnit2));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(List.of(regulatorTeam));

    List<OrganisationUnitJson> singleTestOu = organisationUnitSearchService.searchOrganisationUnitsForUser("2",
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(singleTestOu).containsExactly(orgUnit2Json);
  }

  @Test
  void searchOrganisationUnitsForUser_consulteeUser_allTestOus() {
    when(organisationApi.searchOrganisationUnits(eq("oU"), any(), any()))
        .thenReturn(orgUnits);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, ALLOCATOR_PERMISSION_SET))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, ALLOCATOR_PERMISSION_SET))
        .thenReturn(List.of(consulteeTeam));

    List<OrganisationUnitJson> allTestOus = organisationUnitSearchService.searchOrganisationUnitsForUser("oU",
        ORG_UNITS_SERVICE_PURPOSE, USER, ALLOCATE_CONSULTATION);
    assertThat(allTestOus).containsExactly(orgUnit1Json, orgUnit2Json, orgUnit3Json);
  }

  @Test
  void searchOrganisationUnitsForUser_consulteeUser_singleTestOu() {
    when(organisationApi.searchOrganisationUnits(eq("2"), any(), any()))
        .thenReturn(List.of(orgUnit2));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, ALLOCATOR_PERMISSION_SET))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, ALLOCATOR_PERMISSION_SET))
        .thenReturn(List.of(consulteeTeam));

    List<OrganisationUnitJson> singleTestOu = organisationUnitSearchService.searchOrganisationUnitsForUser("2",
        ORG_UNITS_SERVICE_PURPOSE, USER, ALLOCATE_CONSULTATION);
    assertThat(singleTestOu).containsExactly(orgUnit2Json);
  }

  @Test
  void searchOrganisationUnitsForUser_industryUser_permissionForOneOrgUnit() {
    var searchTerm = "oU";
    when(organisationApi.searchOrganisationUnits(eq(searchTerm), any(), any()))
        .thenReturn(orgUnits);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, CREATOR_PERMISSION_SET))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, CREATOR_PERMISSION_SET))
        .thenReturn(List.of(orgUnit1Json));

    var foundOus = organisationUnitSearchService.searchOrganisationUnitsForUser(searchTerm,
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
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, CREATOR_PERMISSION_SET))
        .thenReturn(List.of());

    var foundOus = organisationUnitSearchService.searchOrganisationUnitsForUser(searchTerm,
        ORG_UNITS_SERVICE_PURPOSE, USER, CREATE_FCS_APPLICATIONS);
    assertThat(foundOus).isEmpty();
  }
}
