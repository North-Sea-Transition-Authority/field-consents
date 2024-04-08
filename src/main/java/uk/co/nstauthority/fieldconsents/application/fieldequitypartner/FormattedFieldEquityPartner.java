package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;

public record FormattedFieldEquityPartner(
    String organisationUnitName,
    String registeredNumber
) implements Comparable<FormattedFieldEquityPartner> {

  public static FormattedFieldEquityPartner from(FieldEquityPartner fieldEquityPartner) {
    var organisationUnit = fieldEquityPartner.getOrganisationUnit();

    if (organisationUnit == null) {
      throw new NullPointerException("OrganisationUnit is null in FieldEquityPartner. Is it included in the EPA query?");
    }

    var organisationUnitName = organisationUnit.getName();

    // This column is non-null in the energy portal database, so a hard error should be thrown
    if (organisationUnitName == null) {
      throw new IllegalStateException(
          "Organisation unit [%s] is missing a name".formatted(organisationUnit.getOrganisationUnitId())
      );
    }

    return new FormattedFieldEquityPartner(organisationUnitName, organisationUnit.getRegisteredNumber());
  }

  public String getFormattedValue() {
    return registeredNumber != null
        ? "%s (%s)".formatted(organisationUnitName, registeredNumber)
        : organisationUnitName;
  }

  @Override
  public int compareTo(FormattedFieldEquityPartner other) {
    return organisationUnitName.compareTo(other.organisationUnitName);
  }
}
