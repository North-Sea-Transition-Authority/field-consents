package uk.co.nstauthority.fieldconsents.assets.facility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.generated.client.FacilitiesByNameAndTypesProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FacilityProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Facility;
import uk.co.fivium.energyportalapi.generated.types.FacilityStatus;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

  private static final Set<FacilityType> ALLOWED_FACILITY_TYPES = EnumSet.of(
      FacilityType.FLOATING_PROCESS_STORAGE_OFFLOADING_UNIT,
      FacilityType.RIGID_FLARE_TOWER,
      FacilityType.FLOATING_STORAGE_UNIT,
      FacilityType.FLOATING_SINGLE_WELL_OPERATION_PRODUCTION_SYSTEM,
      FacilityType.FLARE_TOWER,
      FacilityType.HEAVY_LIFT_VESSEL,
      FacilityType.MOBILE_DRILLING_JACKUP,
      FacilityType.MOBILE_DRILLING_OTHER_TYPE,
      FacilityType.MOBILE_DRILLING_SHIP,
      FacilityType.MOBILE_DRILLING_SEMI_SUBMERSIBLE,
      FacilityType.ARTICULATED_OFFSHORE_LOADING_UNIT,
      FacilityType.BUOY_OFFSHORE_LOADING_UNIT,
      FacilityType.OFFSHORE_STORAGE_UNIT,
      FacilityType.OSPAR_OFFSHORE_LOADING_STORAGE_PLATFORM,
      FacilityType.CONCRETE_GRAVITY_BASED_PLATFORM,
      FacilityType.JACKUP_WITH_CONCRETE_BASE_PLATFORM,
      FacilityType.JACKUP_PLATFORM,
      FacilityType.LARGE_STEEL_PLATFORM,
      FacilityType.DEROGATION_LARGE_STEEL_PLATFORM,
      FacilityType.MONO_TOWER_PLATFORM,
      FacilityType.SMALL_STEEL_PLATFORM,
      FacilityType.TENSION_LEG_PLATFORM,
      FacilityType.SUPPLY_VESSEL,
      FacilityType.TERMINAL,
      FacilityType.WIND_TURBINE,
      FacilityType.WELL_INTERVENTION_VESSEL
  );

  private static final Set<FacilityStatus> ALLOWED_FACILITY_STATUSES = EnumSet.of(
      FacilityStatus.PLANNED,
      FacilityStatus.UNDER_CONSTRUCTION,
      FacilityStatus.OPERATIONAL,
      FacilityStatus.UNKNOWN
  );

  private static final Facility FACILITY =
      Facility.newBuilder()
          .id(1)
          .name("facility name")
          .type(ALLOWED_FACILITY_TYPES.stream().findFirst().orElseThrow())
          .status(ALLOWED_FACILITY_STATUSES.stream().findFirst().orElseThrow())
          .operator(OrganisationUnit.newBuilder()
              .organisationUnitId(100)
              .name("facility operator")
              .build()
          )
          .build();

  private static final FacilityWithOperatorJson FACILITY_WITH_OPERATOR_JSON = FacilityWithOperatorJson.from(FACILITY);

  private static final Map<String, Object> EXPECTED_QUERY_FIELDS = new FacilityProjectionRoot()
      .id()
      .name()
        .status()
        .root()
      .operator()
        .organisationUnitId()
        .name()
        .root()
      .getFields();

  @Mock
  private FacilityApi facilityApi;

  @InjectMocks
  private FacilityService facilityService;

  @Test
  void searchFacilities() {
    var queryCaptor = ArgumentCaptor.forClass(FacilitiesByNameAndTypesProjectionRoot.class);
    var requestPurpose = "request purpose";

    when(facilityApi.searchFacilitiesByNameAndTypeIn(
        eq(FACILITY.getName()),
        eq(ALLOWED_FACILITY_TYPES.stream().toList()),
        queryCaptor.capture(),
        eq(new RequestPurpose(requestPurpose)))
    )
        .thenReturn(List.of(FACILITY));

    assertThat(facilityService.searchFacilities(FACILITY.getName(), requestPurpose)).containsExactly(FACILITY_WITH_OPERATOR_JSON);

    assertThat(queryCaptor.getValue().getFields())
        .usingRecursiveComparison()
        .isEqualTo(EXPECTED_QUERY_FIELDS);
  }

  @Test
  void searchFacilities_facilityHasInvalidStatus() {
    var invalidStatus = EnumSet.allOf(FacilityStatus.class)
        .stream()
        .filter(status -> !ALLOWED_FACILITY_STATUSES.contains(status))
        .findFirst()
        .orElseThrow();

    var facility = Facility.newBuilder()
        .status(invalidStatus)
        .operator(FACILITY.getOperator())
        .build();

    when(facilityApi.searchFacilitiesByNameAndTypeIn(any(), any(), any(), any())).thenReturn(List.of(facility));

    assertThat(facilityService.searchFacilities("name", "")).isEmpty();
  }

  @Test
  void searchFacilities_facilityDoesNotHaveOperator() {
    var facility = Facility.newBuilder()
        .status(ALLOWED_FACILITY_STATUSES.stream().findFirst().orElseThrow())
        .build();

    when(facilityApi.searchFacilitiesByNameAndTypeIn(any(), any(), any(), any())).thenReturn(List.of(facility));

    assertThat(facilityService.searchFacilities("name", "")).isEmpty();
  }

  @Test
  void findFacilityWithOperator() {
    var queryCaptor = ArgumentCaptor.forClass(FacilityProjectionRoot.class);
    var requestPurpose = "request purpose";

    when(facilityApi.findFacilityById(
        eq(FACILITY.getId()),
        queryCaptor.capture(),
        eq(new RequestPurpose(requestPurpose)))
    )
        .thenReturn(Optional.of(FACILITY));

    assertThat(facilityService.findFacilityWithOperator(FACILITY.getId(), requestPurpose)).contains(FACILITY_WITH_OPERATOR_JSON);

    assertThat(queryCaptor.getValue().getFields())
        .usingRecursiveComparison()
        .isEqualTo(EXPECTED_QUERY_FIELDS);
  }

  @Test
  void findFacilityWithOperator_facilityHasInvalidStatus() {
    var invalidStatus = EnumSet.allOf(FacilityStatus.class)
        .stream()
        .filter(status -> !ALLOWED_FACILITY_STATUSES.contains(status))
        .findFirst()
        .orElseThrow();

    var facility = Facility.newBuilder()
        .id(FACILITY.getId())
        .status(invalidStatus)
        .operator(FACILITY.getOperator())
        .build();

    when(facilityApi.findFacilityById(eq(facility.getId()), any(), any())).thenReturn(Optional.of(facility));
    assertThat(facilityService.findFacilityWithOperator(facility.getId(), "")).isEmpty();
  }

  @Test
  void findFacilityWithOperator_facilityDoesNotHaveOperator() {
    var facility = Facility.newBuilder()
        .id(FACILITY.getId())
        .status(ALLOWED_FACILITY_STATUSES.stream().findFirst().orElseThrow())
        .build();

    when(facilityApi.findFacilityById(eq(facility.getId()), any(), any())).thenReturn(Optional.of(facility));

    assertThat(facilityService.findFacilityWithOperator(facility.getId(), "")).isEmpty();
  }

  @Test
  void getFacilityWithOperator() {
    when(facilityApi.findFacilityById(eq(FACILITY.getId()), any(), any())).thenReturn(Optional.of(FACILITY));
    assertThat(facilityService.getFacilityWithOperator(FACILITY.getId(), "")).isEqualTo(FACILITY_WITH_OPERATOR_JSON);
  }

  @Test
  void getFacilityWithOperator_notFound() {
    when(facilityApi.findFacilityById(eq(FACILITY.getId()), any(), any())).thenReturn(Optional.empty());
    var facilityId = FACILITY.getId();
    assertThatThrownBy(() -> facilityService.getFacilityWithOperator(facilityId, ""))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Facility [%s] not found", FACILITY.getId());
  }
}