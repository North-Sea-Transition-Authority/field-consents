package uk.co.nstauthority.fieldconsents.fee;

import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

public record FeeLineView(
    FeeLineMnemonic mnemonic,
    String formattedAmount
) {

  public static FeeLineView from(FeeLineDto feeLineDto) {
    return new FeeLineView(
        FeeLineMnemonic.from(feeLineDto.mnemonic()),
        DecimalFormatUtils.formatMoney((double) feeLineDto.amountPence() / 100)
    );
  }
}
