package uk.co.nstauthority.fieldconsents.assets.hubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Hub;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public record HubWithOperatorJson(
    Integer id,
    String name,
    OrganisationUnitJson organisationUnitJson
) implements AssetWithOperatorJson {

  public static final String SUFFIX = "(HUB)";

  private static final Logger LOGGER = LoggerFactory.getLogger(HubWithOperatorJson.class);

  public static HubWithOperatorJson from(Hub hub) {
    return new HubWithOperatorJson(
        hub.getId(),
        hub.getName(),
        OrganisationUnitJson.from(hub.getOperator())
    );
  }

  public static HubWithOperatorJson fromCachedInformation(Integer hubId, String hubName) {
    LOGGER.warn("Had to fallback to hub cache info for: id {}, name {}", hubId, hubName);
    return new HubWithOperatorJson(hubId, hubName, null);
  }

  @Override
  public OrganisationUnitJson getOperatorJson() {
    return organisationUnitJson;
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
    return null;
  }

  @Override
  public AssetType getAssetType() {
    return AssetType.HUB;
  }

  @Override
  public String getSelectionText() {
    return "%s %s".formatted(name, SUFFIX);
  }
}
