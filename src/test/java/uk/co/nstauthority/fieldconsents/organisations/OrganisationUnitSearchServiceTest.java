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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitSearchServiceTest {

  private static final String ORG_UNITS_SERVICE_PURPOSE = "Org unit service test purpose";

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationApi organisationApi;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private OrganisationUnitSearchService organisationUnitSearchService;

  @Test
  void searchOrganisationUnitsForUser_regulatorUser_allTestOus() {
    when(organisationApi.searchOrganisationUnits(eq("oU"), any(), any()))
        .thenReturn(orgUnits);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, Set.of(Role.CREATOR)))
        .thenReturn(true);

    List<OrganisationUnitJson> allTestOus = organisationUnitSearchService.searchOrganisationUnitsForUser("oU",
        ORG_UNITS_SERVICE_PURPOSE, USER, Set.of(Role.CREATOR));
    assertThat(allTestOus).containsExactly(orgUnit1Json, orgUnit2Json, orgUnit3Json);
  }

  @Test
  void searchOrganisationUnitsForUser_regulatorUser_singleTestOu() {
    when(organisationApi.searchOrganisationUnits(eq("2"), any(), any()))
        .thenReturn(List.of(orgUnit2));

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, Set.of(Role.CREATOR)))
        .thenReturn(true);

    List<OrganisationUnitJson> singleTestOu = organisationUnitSearchService.searchOrganisationUnitsForUser("2",
        ORG_UNITS_SERVICE_PURPOSE, USER, Set.of(Role.CREATOR));
    assertThat(singleTestOu).containsExactly(orgUnit2Json);
  }

  @Test
  void searchOrganisationUnitsForUser_consulteeUser_allTestOus() {
    when(organisationApi.searchOrganisationUnits(eq("oU"), any(), any()))
        .thenReturn(orgUnits);

    var requiredRoles = Set.of(Role.ALLOCATOR);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, requiredRoles))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, requiredRoles))
        .thenReturn(true);

    List<OrganisationUnitJson> allTestOus = organisationUnitSearchService.searchOrganisationUnitsForUser("oU",
        ORG_UNITS_SERVICE_PURPOSE, USER, requiredRoles);
    assertThat(allTestOus).containsExactly(orgUnit1Json, orgUnit2Json, orgUnit3Json);
  }

  @Test
  void searchOrganisationUnitsForUser_consulteeUser_singleTestOu() {
    when(organisationApi.searchOrganisationUnits(eq("2"), any(), any()))
        .thenReturn(List.of(orgUnit2));

    var requiredRoles = Set.of(Role.ALLOCATOR);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, requiredRoles))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, requiredRoles))
        .thenReturn(true);

    List<OrganisationUnitJson> singleTestOu = organisationUnitSearchService.searchOrganisationUnitsForUser("2",
        ORG_UNITS_SERVICE_PURPOSE, USER, Set.of(Role.ALLOCATOR));
    assertThat(singleTestOu).containsExactly(orgUnit2Json);
  }

  @Test
  void searchOrganisationUnitsForUser_industryUser_permissionForOneOrgUnit() {
    var searchTerm = "oU";
    when(organisationApi.searchOrganisationUnits(eq(searchTerm), any(), any()))
        .thenReturn(orgUnits);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, Set.of(Role.CREATOR)))
        .thenReturn(List.of(orgUnit1Json));

    var foundOus = organisationUnitSearchService.searchOrganisationUnitsForUser(searchTerm,
        ORG_UNITS_SERVICE_PURPOSE, USER, Set.of(Role.CREATOR));
    assertThat(foundOus).containsExactly(orgUnit1Json);
  }

  @Test
  void searchOrganisationUnitsForUser_industryUser_permissionForNoOrgUnits() {
    var searchTerm = "oU";
    when(organisationApi.searchOrganisationUnits(eq(searchTerm), any(), any()))
        .thenReturn(orgUnits);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, Set.of(Role.CREATOR)))
        .thenReturn(List.of());

    var foundOus = organisationUnitSearchService.searchOrganisationUnitsForUser(searchTerm,
        ORG_UNITS_SERVICE_PURPOSE, USER, Set.of(Role.CREATOR));
    assertThat(foundOus).isEmpty();
  }
}
