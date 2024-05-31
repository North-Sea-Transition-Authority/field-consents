package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.format;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class ConsentDataViewTest {

  @Test
  void fromShortTermOrAnnualProductionApplication() {
    var consentData = ConsentDataTestUtil.newBuilder()
        .withShortTermOrAnnualProductionMinOil(BigDecimal.valueOf(428.76))
        .withShortTermOrAnnualProductionMaxOil(BigDecimal.valueOf(714.23))
        .withShortTermOrAnnualProductionMinGas(BigDecimal.valueOf(189.47))
        .withShortTermOrAnnualProductionMaxGas(BigDecimal.valueOf(837.14))
        .build();

    assertThat(ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData))
        .isEqualTo(
            new ConsentDataView(
                format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
                format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
                null,
                ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData),
                null,
                null,
                null
            )
        );
  }

  @Test
  void fromLongTermProductionApplication() {
    var longTermProductionConsentProductionFromDate = LocalDate.parse("2024-02-23");

    var consentData = ConsentDataTestUtil.newBuilder()
        .withLongTermProductionConsentProductionFromDate(longTermProductionConsentProductionFromDate)
        .build();

    var consentDataLongTermProductionFiguresViews = Map.of(
        "2024", mock(ConsentProductionFiguresView.class),
        "2025", mock(ConsentProductionFiguresView.class)
    );

    assertThat(ConsentDataView.fromLongTermProductionApplication(consentData, consentDataLongTermProductionFiguresViews)).isEqualTo(
        new ConsentDataView(
            format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
            format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
            format(longTermProductionConsentProductionFromDate, DateUtils.LONG_DATE),
            null,
            consentDataLongTermProductionFiguresViews,
            null,
            null
        )
    );
  }

  @Test
  void fromShortTermOrAnnualEmissionApplication() {
    var emissionDailyAverage = BigDecimal.valueOf(235.79);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(emissionDailyAverage)
        .build();

    assertThat(ConsentDataView.fromShortTermOrAnnualEmissionApplication(consentData)).isEqualTo(
        new ConsentDataView(
            format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
            format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
            null,
            null,
            null,
            bigDecimalToFormattedString(emissionDailyAverage),
            null
        )
    );
  }

  @Test
  void fromLongTermEmissionApplication() {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentDataLongTermEmissionFiguresViews = Map.of(
        "2024", "1.123456",
        "2025", "987.129"
    );

    assertThat(ConsentDataView.fromLongTermEmissionApplication(consentData, consentDataLongTermEmissionFiguresViews))
        .isEqualTo(
            new ConsentDataView(
                format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
                format(consentData.getConsentEndDate(), DateUtils.LONG_DATE),
                null,
                null,
                null,
                null,
                consentDataLongTermEmissionFiguresViews
            )
        );
  }
}
