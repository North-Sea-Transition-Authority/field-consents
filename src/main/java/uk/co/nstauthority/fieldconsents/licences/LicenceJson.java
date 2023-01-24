package uk.co.nstauthority.fieldconsents.licences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Licence;

public record LicenceJson(
    Integer licenceId,
    String licenceRef
) {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceJson.class);

  public static LicenceJson from(Licence licence) {
    return new LicenceJson(licence.getId(), licence.getLicenceRef());
  }

  public static LicenceJson fromCachedInformation(Integer licenceId, String licenceRef) {
    LOGGER.debug("Using licence cache info for: id {}, ref {}", licenceId, licenceRef);
    return new LicenceJson(licenceId, licenceRef);
  }
}
