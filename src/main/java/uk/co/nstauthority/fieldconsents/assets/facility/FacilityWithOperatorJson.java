package uk.co.nstauthority.fieldconsents.assets.facility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Facility;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public record FacilityWithOperatorJson(
    Integer id,
    String name,
    String statusDisplayName,
    OrganisationUnitJson organisationUnitJson
) implements AssetWithOperatorJson {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityWithOperatorJson.class);

  public static FacilityWithOperatorJson from(Facility facility) {
    return new FacilityWithOperatorJson(
        facility.getId(),
        facility.getName(),
        facility.getStatus().name(),
        OrganisationUnitJson.from(facility.getOperator())
    );
  }

  public static FacilityWithOperatorJson fromCachedInformation(Integer facilityId, String facilityName) {
    LOGGER.warn("Had to fallback to facility cache info for: id {}, name {}", facilityId, facilityName);
    return new FacilityWithOperatorJson(facilityId, facilityName, null, null);
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getStatusDisplayName() {
    return statusDisplayName;
  }

  @Override
  public AssetType getAssetType() {
    return AssetType.FACILITY;
  }

  @Override
  public OrganisationUnitJson getOperatorJson() {
    return organisationUnitJson;
  }
}
