package uk.co.nstauthority.fieldconsents.fee;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

class FeeLineViewTest {

  @Test
  void from() {
    var feeLineDto
        = new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 100);

    var view = FeeLineView.from(feeLineDto);

    assertThat(view).isEqualTo(
        new FeeLineView(
            FeeLineMnemonic.from(feeLineDto.mnemonic()),
            DecimalFormatUtils.formatMoney((double) feeLineDto.amountPence() / 100)
        )
    );
  }
}
