package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import java.util.List;

public record FieldEquityPartnersView(
    List<FormattedFieldEquityPartner> formattedFieldEquityPartners,
    List<String> organisationGroupNamesWithoutConsentRecipients
) {

}
