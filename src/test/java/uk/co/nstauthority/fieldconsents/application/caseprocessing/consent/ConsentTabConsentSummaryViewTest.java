package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

class ConsentTabConsentSummaryViewTest {

  @Test
  void from() {
    var consent = ConsentTestUtil.newBuilder().build();
    var issuedByUser = new ServiceUserDetail(null, null, "testForename", "testSurname", "testEmailAddress");
    var summaryFileViews = List.of(mock(SummaryFileView.class));

    assertThat(ConsentTabConsentSummaryView.from(consent, issuedByUser, summaryFileViews)).isEqualTo(
        new ConsentTabConsentSummaryView(
            issuedByUser.displayNameAndEmail(),
            DateUtils.format(consent.getIssuedInstant(), DateUtils.DATE_TIME),
            summaryFileViews
        )
    );
  }
}
