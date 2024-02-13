package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import java.util.List;

public class FieldEquityPartnersViewTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private List<String> fieldEquityPartnerNames = List.of("BP", "SHELL");
    private List<String> organisationGroupsWithoutConsentRecipients = List.of("BP");

    public Builder withFieldEquityPartnerNames(List<String> fieldEquityPartnerNames) {
      this.fieldEquityPartnerNames = fieldEquityPartnerNames;
      return this;
    }

    public Builder withOrganisationGroupsWithoutConsentRecipients(List<String> organisationGroupsWithoutConsentRecipients) {
      this.organisationGroupsWithoutConsentRecipients = organisationGroupsWithoutConsentRecipients;
      return this;
    }

    public FieldEquityPartnersView build() {
      return new FieldEquityPartnersView(
          fieldEquityPartnerNames,
          organisationGroupsWithoutConsentRecipients
      );
    }

    private Builder() {
    }
  }


}
