package uk.co.nstauthority.fieldconsents.licences;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class LicenceJsonComparator implements Comparator<LicenceJson> {

  @Override
  public int compare(LicenceJson firstLicence, LicenceJson secondLicence) {
    return Comparator.comparing(byLicenceType())
        .thenComparing(byLicenceNumber())
        .compare(firstLicence, secondLicence);
  }

  private Function<LicenceJson, String> byLicenceType() {
    return licence ->
        Optional.ofNullable(licence.licenceType())
            .map(String::toLowerCase)
            .orElse("");
  }

  private Function<LicenceJson, Integer> byLicenceNumber() {
    return licence ->
        Optional.ofNullable(licence.licenceNo())
            .orElse(0);
  }
}
