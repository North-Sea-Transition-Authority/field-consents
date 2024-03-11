package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class ConsentIssuingApprovalSummaryViewTest {

  @Test
  void from() {
    var consentIssuingApproval = ConsentIssuingApprovalTestUtil.newBuilder().build();
    var approvedByUser = ServiceUserDetailTestUtil.Builder().build();

    assertThat(ConsentIssuingApprovalSummaryView.from(consentIssuingApproval, approvedByUser)).isEqualTo(
        new ConsentIssuingApprovalSummaryView(
            approvedByUser.displayNameAndEmail(),
            DateUtils.format(consentIssuingApproval.getApprovedInstant(), DateUtils.DATE_TIME)
        )
    );
  }
}
