package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;

class ConsentProductionFiguresViewTest {

  @Test
  void fromShortTermOrAnnualConsentProductionFigures() {
    var shortTermOrAnnualProductionMinOil = BigDecimal.valueOf(235.79);
    var shortTermOrAnnualProductionMaxOil = BigDecimal.valueOf(673.12);
    var shortTermOrAnnualProductionMinGas = BigDecimal.valueOf(112.89);
    var shortTermOrAnnualProductionMaxGas = BigDecimal.valueOf(456.99);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withShortTermOrAnnualProductionMinOil(shortTermOrAnnualProductionMinOil)
        .withShortTermOrAnnualProductionMaxOil(shortTermOrAnnualProductionMaxOil)
        .withShortTermOrAnnualProductionMinGas(shortTermOrAnnualProductionMinGas)
        .withShortTermOrAnnualProductionMaxGas(shortTermOrAnnualProductionMaxGas)
        .build();

    assertThat(ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData)).isEqualTo(
        new ConsentProductionFiguresView(
            bigDecimalToFormattedString(shortTermOrAnnualProductionMinOil),
            bigDecimalToFormattedString(shortTermOrAnnualProductionMaxOil),
            bigDecimalToFormattedString(shortTermOrAnnualProductionMinGas),
            bigDecimalToFormattedString(shortTermOrAnnualProductionMaxGas)
        )
    );
  }

  @Test
  void fromConsentDataLongTermProductionFigures() {
    var consentDataLongTermProductionFigures = ConsentDataLongTermProductionFiguresTestUtil.builder().build();

    assertThat(ConsentProductionFiguresView.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures)).isEqualTo(
        new ConsentProductionFiguresView(
            bigDecimalToFormattedString(consentDataLongTermProductionFigures.getMinOil()),
            bigDecimalToFormattedString(consentDataLongTermProductionFigures.getMaxOil()),
            bigDecimalToFormattedString(consentDataLongTermProductionFigures.getMinGas()),
            bigDecimalToFormattedString(consentDataLongTermProductionFigures.getMaxGas())
        )
    );
  }
}
