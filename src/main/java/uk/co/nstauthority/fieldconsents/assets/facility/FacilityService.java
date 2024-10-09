package uk.co.nstauthority.fieldconsents.assets.facility;

import jakarta.persistence.EntityNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.generated.client.FacilitiesByNameAndTypesProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FacilityProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Facility;
import uk.co.fivium.energyportalapi.generated.types.FacilityStatus;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;

@Service
public class FacilityService {

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

  private final FacilityApi facilityApi;

  FacilityService(FacilityApi facilityApi) {
    this.facilityApi = facilityApi;
  }

  public List<FacilityWithOperatorJson> searchFacilities(String name, String requestPurpose) {
    var query = new FacilitiesByNameAndTypesProjectionRoot()
        .id()
        .name()
        .status()
          .root()
        .operator()
          .organisationUnitId()
          .name()
          .root();

    var types = ALLOWED_FACILITY_TYPES.stream().toList();
    return facilityApi.searchFacilitiesByNameAndTypeIn(name, types, query, new RequestPurpose(requestPurpose))
        .stream()
        .filter(this::facilityHasValidStatus)
        .filter(this::facilityHasOperator)
        .map(FacilityWithOperatorJson::from)
        .toList();
  }

  public Optional<FacilityWithOperatorJson> findFacilityWithOperator(Integer facilityId, String requestPurpose) {
    var query = new FacilityProjectionRoot()
        .id()
        .name()
        .status()
          .root()
        .operator()
          .organisationUnitId()
          .name()
          .root();

    return facilityApi.findFacilityById(facilityId, query, new RequestPurpose(requestPurpose))
        .filter(this::facilityHasValidStatus)
        .filter(this::facilityHasOperator)
        .map(FacilityWithOperatorJson::from);
  }

  public FacilityWithOperatorJson getFacilityWithOperator(Integer facilityId, String requestPurpose) {
    return findFacilityWithOperator(facilityId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Facility [%d] not found".formatted(facilityId)));
  }

  private boolean facilityHasValidStatus(Facility facility) {
    return ALLOWED_FACILITY_STATUSES.contains(facility.getStatus());
  }

  private boolean facilityHasOperator(Facility facility) {
    var operator = facility.getOperator();
    if (operator == null) {
      return false;
    }

    if (operator.getOrganisationUnitId() == null) {
      return false;
    }

    return operator.getName() != null;
  }

}
