package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

public class FormattedFieldEquityPartnerTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String organisationUnitName = "BP";
    private String registeredNumber = "123";

    private Builder() {

    }

    public Builder withOrganisationUnitName(String organisationUnitName) {
      this.organisationUnitName = organisationUnitName;
      return this;
    }

    public Builder withRegisteredNumber(String registeredNumber) {
      this.registeredNumber = registeredNumber;
      return this;
    }

    public FormattedFieldEquityPartner build() {
      return new FormattedFieldEquityPartner(
          organisationUnitName,
          registeredNumber
      );
    }

  }

}
