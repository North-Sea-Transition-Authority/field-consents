package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.format;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class ConsentDataViewTest {

  @Test
  void fromShortTermOrAnnualProductionApplication() {
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var shortTermOrAnnualConsentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    assertThat(
        ConsentDataView.fromShortTermOrAnnualProductionApplication(
            consentData,
            shortTermOrAnnualConsentProductionFiguresDto
        )
    ).isEqualTo(
        new ConsentDataView(
            format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
            format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
            ConsentProductionFiguresView.from(shortTermOrAnnualConsentProductionFiguresDto),
            null,
            null
        )
    );
  }

  @Test
  void fromLongTermProductionApplication() {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var productionFigures2024 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(428.76))
        .withMaxOil(BigDecimal.valueOf(714.23))
        .withMinGas(BigDecimal.valueOf(189.47))
        .withMaxGas(BigDecimal.valueOf(837.14))
        .build();
    var productionFigures2025 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(583.24))
        .withMaxOil(BigDecimal.valueOf(327.89))
        .withMinGas(BigDecimal.valueOf(901.45))
        .withMaxGas(BigDecimal.valueOf(124.56))
        .build();
    var productionFigures2026 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(241.57))
        .withMaxOil(BigDecimal.valueOf(789.32))
        .withMinGas(BigDecimal.valueOf(456.28))
        .withMaxGas(BigDecimal.valueOf(602.11))
        .build();
    var productionFigures2027 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(147.83))
        .withMaxOil(BigDecimal.valueOf(562.39))
        .withMinGas(BigDecimal.valueOf(378.21))
        .withMaxGas(BigDecimal.valueOf(943.67))
        .build();
    var productionFigures2028 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(689.45))
        .withMaxOil(BigDecimal.valueOf(235.78))
        .withMinGas(BigDecimal.valueOf(501.92))
        .withMaxGas(BigDecimal.valueOf(789.34))
        .build();

    var longTermConsentProductionFigures = Map.of(
        2026, productionFigures2026,
        2028, productionFigures2028,
        2025, productionFigures2025,
        2024, productionFigures2024,
        2027, productionFigures2027
    );

    assertThat(ConsentDataView.fromLongTermProductionApplication(consentData, longTermConsentProductionFigures)).isEqualTo(
        new ConsentDataView(
            format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
            format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
            null,
            Map.of(
                "2024", ConsentProductionFiguresView.from(productionFigures2024),
                "2025", ConsentProductionFiguresView.from(productionFigures2025),
                "2026", ConsentProductionFiguresView.from(productionFigures2026),
                "2027", ConsentProductionFiguresView.from(productionFigures2027),
                "2028", ConsentProductionFiguresView.from(productionFigures2028)
            ),
            null
        )
    );
  }

  @Test
  void fromFlareOrVentApplication() {
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var emissionMaxRate = BigDecimal.valueOf(235.79);

    assertThat(ConsentDataView.fromFlareOrVentApplication(consentData, emissionMaxRate)).isEqualTo(
        new ConsentDataView(
            format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
            format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
            null,
            null,
            bigDecimalToFormattedString(emissionMaxRate)
        )
    );
  }
}
