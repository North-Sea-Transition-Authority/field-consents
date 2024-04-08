package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import java.util.List;

public class FieldEquityPartnersViewTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private List<FormattedFieldEquityPartner> formattedFieldEquityPartners = List.of(
        FormattedFieldEquityPartnerTestUtil.newBuilder().build(),
        FormattedFieldEquityPartnerTestUtil.newBuilder().withOrganisationUnitName("SHELL").withRegisteredNumber("100").build()
    );
    private List<String> organisationGroupsWithoutConsentRecipients = List.of("SHELL GROUP");

    public Builder withFormattedFieldEquityPartners(List<FormattedFieldEquityPartner> formattedFieldEquityPartners) {
      this.formattedFieldEquityPartners = formattedFieldEquityPartners;
      return this;
    }

    public Builder withOrganisationGroupsWithoutConsentRecipients(List<String> organisationGroupsWithoutConsentRecipients) {
      this.organisationGroupsWithoutConsentRecipients = organisationGroupsWithoutConsentRecipients;
      return this;
    }

    public FieldEquityPartnersView build() {
      return new FieldEquityPartnersView(
          formattedFieldEquityPartners,
          organisationGroupsWithoutConsentRecipients
      );
    }

    private Builder() {
    }
  }


}
