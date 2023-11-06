package uk.co.nstauthority.fieldconsents.application.caseprocessing.payment;

import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

public record PaymentsTabPaymentSummaryView(
    String status,
    String description,
    String formattedPaymentAmount,
    String paidByUser,
    String formattedPaymentDate,
    String govUkPayReference
) {

  static final String PAID_STATUS = "Paid";

  static PaymentsTabPaymentSummaryView from(PaymentDto paymentDto, ServiceUserDetail createdByUser) {
    var paidByUser = "%s (%s)".formatted(createdByUser.displayName(), createdByUser.emailAddress());
    var formattedPaymentAmount = DecimalFormatUtils.formatMoney((double) paymentDto.amountPence() / 100);
    var formattedPaymentDate = DateUtils.format(paymentDto.govUkPayCaptureSubmitInstant(), DateUtils.DATE_TIME);

    return new PaymentsTabPaymentSummaryView(
        PAID_STATUS,
        paymentDto.description(),
        formattedPaymentAmount,
        paidByUser,
        formattedPaymentDate,
        paymentDto.itemReference()
    );
  }
}
