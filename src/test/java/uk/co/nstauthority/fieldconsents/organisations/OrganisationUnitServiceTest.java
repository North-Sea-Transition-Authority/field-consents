package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnits;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitServiceTest {

  private static final String ORG_UNITS_SERVICE_PURPOSE = "Org unit service test purpose";

  OrganisationUnitService organisationUnitService;

  @Mock
  OrganisationApi organisationApi;

  @BeforeEach
  void setup() {
    organisationUnitService = new OrganisationUnitService(organisationApi);
  }

  @Test
  void searchOrganisationUnits_allTestOus() {
    when(organisationApi.searchOrganisationUnits(eq("oU"), any(), any()))
        .thenReturn(orgUnits);

    List<OrganisationUnitJson> allTestOus = organisationUnitService.searchOrganisationUnits("oU",
        ORG_UNITS_SERVICE_PURPOSE);
    assertThat(allTestOus).containsExactly(orgUnit1Json, orgUnit2Json, orgUnit3Json);
  }

  @Test
  void searchOrganisationUnits_singleTestOu() {
    when(organisationApi.searchOrganisationUnits(eq("2"), any(), any()))
        .thenReturn(List.of(orgUnit2));

    List<OrganisationUnitJson> singleTestOu = organisationUnitService.searchOrganisationUnits("2",
        ORG_UNITS_SERVICE_PURPOSE);
    assertThat(singleTestOu).containsExactly(orgUnit2Json);
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

}
