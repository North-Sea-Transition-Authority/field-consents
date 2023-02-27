package uk.co.nstauthority.fieldconsents.petsapplications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.PetsApplication;
import uk.co.fivium.energyportalapi.generated.types.SatDecision;
import uk.co.fivium.energyportalapi.generated.types.SatStatus;
import uk.co.fivium.energyportalapi.generated.types.SatType;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record PetsApplicationJson(
    Integer satId,
    String satRef,
    SatType satType,
    SatStatus status,
    SatDecision decision
) implements SearchSelectable {

  private static final Logger LOGGER = LoggerFactory.getLogger(PetsApplicationJson.class);

  public static PetsApplicationJson from(PetsApplication petsApplication) {
    return new PetsApplicationJson(
        petsApplication.getSatId(),
        petsApplication.getSatRef(),
        petsApplication.getSatType(),
        petsApplication.getStatus(),
        petsApplication.getDecision()
    );
  }

  public static PetsApplicationJson fromCachedInformation(Integer satId, String cachedSatRef) {
    LOGGER.warn("Had to fallback to pets application cache info for: satId {}, satRef {}", satId, cachedSatRef);
    return new PetsApplicationJson(satId, cachedSatRef, null, null, null);
  }

  @Override
  public String getSelectionId() {
    return this.satId.toString();
  }

  @Override
  public String getSelectionText() {
    return this.satRef;
  }
}