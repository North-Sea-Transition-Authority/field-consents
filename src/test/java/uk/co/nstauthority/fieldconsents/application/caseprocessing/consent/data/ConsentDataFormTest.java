package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionLongTermFiguresTestUtil;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

class ConsentDataFormTest {

  @Test
  void fromShortTermOrAnnualProductionApplication_withValues() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");
    var shortTermOrAnnualConsentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(428.76))
        .withMaxOil(BigDecimal.valueOf(714.23))
        .withMinGas(BigDecimal.valueOf(189.47))
        .withMaxGas(BigDecimal.valueOf(837.14))
        .build();

    assertThat(
        ConsentDataForm.fromShortTermOrAnnualProductionApplication(
            startDate,
            endDate,
            shortTermOrAnnualConsentProductionFiguresDto
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getShortTermOrAnnualConsentProductionFiguresInput().getAsDtoOrThrow()
        )
        .containsExactly(
            startDate,
            endDate,
            shortTermOrAnnualConsentProductionFiguresDto
        );
  }

  @Test
  void fromShortTermOrAnnualProductionApplication_withConsentData() {
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

    assertThat(ConsentDataForm.fromShortTermOrAnnualProductionApplication(consentData))
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getShortTermOrAnnualConsentProductionFiguresInput().getAsDtoOrThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate(),
            new ConsentProductionFiguresDto(
                shortTermOrAnnualProductionMinOil,
                shortTermOrAnnualProductionMaxOil,
                shortTermOrAnnualProductionMinGas,
                shortTermOrAnnualProductionMaxGas
            )
        );
  }

  @Test
  void fromLongTermProductionApplication_withValues() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    var consentProductionFiguresDto2024 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(428.76))
        .withMaxOil(BigDecimal.valueOf(714.23))
        .withMinGas(BigDecimal.valueOf(189.47))
        .withMaxGas(BigDecimal.valueOf(837.14))
        .build();
    var consentProductionFiguresDto2025 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(583.24))
        .withMaxOil(BigDecimal.valueOf(327.89))
        .withMinGas(BigDecimal.valueOf(901.45))
        .withMaxGas(BigDecimal.valueOf(124.56))
        .build();
    var consentProductionFiguresDto2026 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(241.57))
        .withMaxOil(BigDecimal.valueOf(789.32))
        .withMinGas(BigDecimal.valueOf(456.28))
        .withMaxGas(BigDecimal.valueOf(602.11))
        .build();
    var consentProductionFiguresDto2027 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(147.83))
        .withMaxOil(BigDecimal.valueOf(562.39))
        .withMinGas(BigDecimal.valueOf(378.21))
        .withMaxGas(BigDecimal.valueOf(943.67))
        .build();
    var consentProductionFiguresDto2028 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(689.45))
        .withMaxOil(BigDecimal.valueOf(235.78))
        .withMinGas(BigDecimal.valueOf(501.92))
        .withMaxGas(BigDecimal.valueOf(789.34))
        .build();

    var longTermConsentProductionFiguresDtos = Map.of(
        2026, consentProductionFiguresDto2026,
        2028, consentProductionFiguresDto2028,
        2025, consentProductionFiguresDto2025,
        2024, consentProductionFiguresDto2024,
        2027, consentProductionFiguresDto2027
    );

    var form = ConsentDataForm.fromLongTermProductionApplication(
        startDate,
        endDate,
        longTermConsentProductionFiguresDtos
    );

    assertThat(form)
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getLongTermProductionConsentProductionFromDateInput().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            startDate,
            endDate,
            startDate
        );

    assertThat(
        form.getLongTermConsentProductionFiguresInputs()
            .entrySet()
            .stream()
            .collect(StreamUtils.toLinkedHashMap(Map.Entry::getKey, entry -> entry.getValue().getAsDtoOrThrow()))
    )
        .containsExactly(
            entry("2024", consentProductionFiguresDto2024),
            entry("2025", consentProductionFiguresDto2025),
            entry("2026", consentProductionFiguresDto2026),
            entry("2027", consentProductionFiguresDto2027),
            entry("2028", consentProductionFiguresDto2028)
        );
  }

  @Test
  void fromLongTermProductionApplication_withConsentData() {
    var longTermProductionConsentProductionFromDate = LocalDate.parse("2024-02-23");

    var consentData = ConsentDataTestUtil.newBuilder()
        .withLongTermProductionConsentProductionFromDate(longTermProductionConsentProductionFromDate)
        .build();

    var consentProductionLongTermFigures2024 = ConsentProductionLongTermFiguresTestUtil.builder()
        .withYear(2024)
        .withMinOil(BigDecimal.valueOf(428.76))
        .withMaxOil(BigDecimal.valueOf(714.23))
        .withMinGas(BigDecimal.valueOf(189.47))
        .withMaxGas(BigDecimal.valueOf(837.14))
        .build();
    var consentProductionLongTermFigures2025 = ConsentProductionLongTermFiguresTestUtil.builder()
        .withYear(2025)
        .withMinOil(BigDecimal.valueOf(583.24))
        .withMaxOil(BigDecimal.valueOf(327.89))
        .withMinGas(BigDecimal.valueOf(901.45))
        .withMaxGas(BigDecimal.valueOf(124.56))
        .build();
    var consentProductionLongTermFigures2026 = ConsentProductionLongTermFiguresTestUtil.builder()
        .withYear(2026)
        .withMinOil(BigDecimal.valueOf(241.57))
        .withMaxOil(BigDecimal.valueOf(789.32))
        .withMinGas(BigDecimal.valueOf(456.28))
        .withMaxGas(BigDecimal.valueOf(602.11))
        .build();
    var consentProductionLongTermFigures2027 = ConsentProductionLongTermFiguresTestUtil.builder()
        .withYear(2027)
        .withMinOil(BigDecimal.valueOf(147.83))
        .withMaxOil(BigDecimal.valueOf(562.39))
        .withMinGas(BigDecimal.valueOf(378.21))
        .withMaxGas(BigDecimal.valueOf(943.67))
        .build();
    var consentProductionLongTermFigures2028 = ConsentProductionLongTermFiguresTestUtil.builder()
        .withYear(2028)
        .withMinOil(BigDecimal.valueOf(689.45))
        .withMaxOil(BigDecimal.valueOf(235.78))
        .withMinGas(BigDecimal.valueOf(501.92))
        .withMaxGas(BigDecimal.valueOf(789.34))
        .build();

    var consentProductionLongTermFiguresList = List.of(
        consentProductionLongTermFigures2026,
        consentProductionLongTermFigures2028,
        consentProductionLongTermFigures2025,
        consentProductionLongTermFigures2024,
        consentProductionLongTermFigures2027
    );

    var form = ConsentDataForm.fromLongTermProductionApplication(consentData, consentProductionLongTermFiguresList);

    assertThat(form)
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getLongTermProductionConsentProductionFromDateInput().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate(),
            longTermProductionConsentProductionFromDate
        );

    assertThat(
        form.getLongTermConsentProductionFiguresInputs()
            .entrySet()
            .stream()
            .collect(StreamUtils.toLinkedHashMap(Map.Entry::getKey, entry -> entry.getValue().getAsDtoOrThrow()))
    )
        .containsExactly(
            entry("2024", ConsentProductionFiguresDto.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2024)),
            entry("2025", ConsentProductionFiguresDto.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2025)),
            entry("2026", ConsentProductionFiguresDto.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2026)),
            entry("2027", ConsentProductionFiguresDto.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2027)),
            entry("2028", ConsentProductionFiguresDto.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2028))
        );
  }

  @Test
  void fromEmissionApplication_withValues() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");
    var emissionDailyAverage = BigDecimal.valueOf(873.46);

    assertThat(
        ConsentDataForm.fromEmissionApplication(
            startDate,
            endDate,
            emissionDailyAverage
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getEmissionDailyAverageInput().getAsBigDecimal().orElseThrow()
        )
        .containsExactly(
            startDate,
            endDate,
            emissionDailyAverage
        );
  }

  @Test
  void fromEmissionApplication_withConsentData() {
    var emissionDailyAverage = BigDecimal.valueOf(873.46);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(emissionDailyAverage)
        .build();

    assertThat(ConsentDataForm.fromEmissionApplication(consentData))
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getEmissionDailyAverageInput().getAsBigDecimal().orElseThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate(),
            emissionDailyAverage
        );
  }
}
