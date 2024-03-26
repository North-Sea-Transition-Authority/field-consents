package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;

class ConsentProductionFiguresDtoTest {

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

    assertThat(ConsentProductionFiguresDto.fromShortTermOrAnnualConsentProductionFigures(consentData)).isEqualTo(
        new ConsentProductionFiguresDto(
            shortTermOrAnnualProductionMinOil,
            shortTermOrAnnualProductionMaxOil,
            shortTermOrAnnualProductionMinGas,
            shortTermOrAnnualProductionMaxGas
        )
    );
  }

  @Test
  void fromConsentDataLongTermProductionFigures() {
    var consentDataLongTermProductionFigures = ConsentDataLongTermProductionFiguresTestUtil.builder().build();

    assertThat(ConsentProductionFiguresDto.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures)).isEqualTo(
        new ConsentProductionFiguresDto(
            consentDataLongTermProductionFigures.getMinOil(),
            consentDataLongTermProductionFigures.getMaxOil(),
            consentDataLongTermProductionFigures.getMinGas(),
            consentDataLongTermProductionFigures.getMaxGas()
        )
    );
  }
}
