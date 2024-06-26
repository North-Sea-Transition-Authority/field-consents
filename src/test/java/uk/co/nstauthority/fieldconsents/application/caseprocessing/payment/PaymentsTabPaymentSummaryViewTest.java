package uk.co.nstauthority.fieldconsents.application.caseprocessing.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

class PaymentsTabPaymentSummaryViewTest {

  @Test
  void from() {
    var paymentDto = mock(PaymentDto.class);
    var createdByUser = ServiceUserDetailTestUtil.Builder().build();

    var paymentDtoAmountPence = 118000;
    var paymentDtoGovUkPayCaptureSubmitInstant = Instant.now();
    var paymentDtoDescription = "testDescription";
    var paymentDtoItemReference = "testItemReference";

    when(paymentDto.amountPence()).thenReturn(paymentDtoAmountPence);
    when(paymentDto.govUkPayCaptureSubmitInstant()).thenReturn(paymentDtoGovUkPayCaptureSubmitInstant);
    when(paymentDto.description()).thenReturn(paymentDtoDescription);
    when(paymentDto.itemReference()).thenReturn(paymentDtoItemReference);

    assertThat(PaymentsTabPaymentSummaryView.from(paymentDto, createdByUser)).isEqualTo(
        new PaymentsTabPaymentSummaryView(
            PaymentsTabPaymentSummaryView.PAID_STATUS,
            paymentDtoDescription,
            DecimalFormatUtils.formatMoney((double) paymentDtoAmountPence / 100),
            createdByUser.displayNameAndEmail(),
            DateUtils.format(paymentDtoGovUkPayCaptureSubmitInstant, DateUtils.DATE_TIME),
            paymentDtoItemReference
        )
    );
  }
}
