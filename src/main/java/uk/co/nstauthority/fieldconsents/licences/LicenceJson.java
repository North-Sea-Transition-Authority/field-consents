package uk.co.nstauthority.fieldconsents.licences;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Licence;

public record LicenceJson(
    Integer licenceId,
    String licenceType,
    Integer licenceNo,
    String licenceRef,
    LocalDate scheduleExpiryDate
) {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceJson.class);

  public static LicenceJson from(Licence licence) {
    return new LicenceJson(
        licence.getId(),
        licence.getLicenceType(),
        licence.getLicenceNo(),
        licence.getLicenceRef(),
        licence.getScheduleExpiryDate()
    );
  }

  public static LicenceJson fromCachedInformation(
      Integer licenceId,
      String licenceType,
      Integer licenceNo,
      String licenceRef,
      LocalDate scheduleExpiryDate
  ) {
    LOGGER.debug(
        "Using licence cache info for: id {}, type {}, no {}, ref {}, expiry date {}",
        licenceId,
        licenceType,
        licenceNo,
        licenceRef,
        scheduleExpiryDate
    );
    return new LicenceJson(
        licenceId,
        licenceType,
        licenceNo,
        licenceRef,
        scheduleExpiryDate
    );
  }
}
