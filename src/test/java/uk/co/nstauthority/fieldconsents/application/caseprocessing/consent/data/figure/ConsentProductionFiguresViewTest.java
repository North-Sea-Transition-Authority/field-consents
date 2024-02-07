package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import org.junit.jupiter.api.Test;

class ConsentProductionFiguresViewTest {

  @Test
  void from() {
    var consentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    assertThat(ConsentProductionFiguresView.from(consentProductionFiguresDto)).isEqualTo(
        new ConsentProductionFiguresView(
            bigDecimalToFormattedString(consentProductionFiguresDto.minOil()),
            bigDecimalToFormattedString(consentProductionFiguresDto.maxOil()),
            bigDecimalToFormattedString(consentProductionFiguresDto.minGas()),
            bigDecimalToFormattedString(consentProductionFiguresDto.maxGas())
        )
    );
  }
}
