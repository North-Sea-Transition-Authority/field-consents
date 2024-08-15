package uk.co.nstauthority.fieldconsents.licences;

import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record LicenceView(
    String licenceRef,
    String scheduleExpiryDate
) {

  public static LicenceView from(LicenceJson licenceJson) {
    return new LicenceView(
        licenceJson.licenceRef(),
        DateUtils.format(licenceJson.scheduleExpiryDate(), DateUtils.LONG_DATE)
    );
  }
}
