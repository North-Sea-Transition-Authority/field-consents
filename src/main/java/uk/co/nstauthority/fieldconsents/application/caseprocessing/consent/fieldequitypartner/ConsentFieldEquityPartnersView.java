package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FormattedFieldEquityPartner;

public record ConsentFieldEquityPartnersView(
    List<FormattedFieldEquityPartner> formattedFieldEquityPartners
) {
}
