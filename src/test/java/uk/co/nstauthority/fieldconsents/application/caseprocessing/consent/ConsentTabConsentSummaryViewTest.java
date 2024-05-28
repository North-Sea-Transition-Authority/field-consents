package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnersView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

class ConsentTabConsentSummaryViewTest {

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void from(ApplicationType applicationType) {
    var consentLengthType = mock(ConsentLengthType.class);
    var issuedInstant = Instant.now();
    var issuedByUser = new ServiceUserDetail(null, null, "testForename", "testSurname", "testEmailAddress");
    var consentStatus = ConsentStatus.ISSUED;
    var consentDataView = mock(ConsentDataView.class);
    var consentFigureUnitView = mock(ConsentFigureUnitView.class);
    var consentFieldEquityPartnersView = mock(ConsentFieldEquityPartnersView.class);
    var summaryFileViews = List.of(mock(SummaryFileView.class));

    assertThat(
        ConsentTabConsentSummaryView.from(
            applicationType,
            consentLengthType,
            issuedByUser,
            issuedInstant,
            consentStatus,
            null,
            consentDataView,
            consentFigureUnitView,
            consentFieldEquityPartnersView,
            summaryFileViews
        )
    ).isEqualTo(
        new ConsentTabConsentSummaryView(
            applicationType,
            consentLengthType,
            issuedByUser.displayNameAndEmail(),
            DateUtils.format(issuedInstant, DateUtils.DATE_TIME),
            consentStatus,
            null,
            consentDataView,
            consentFigureUnitView,
            consentFieldEquityPartnersView,
            summaryFileViews
        )
    );
  }
}
