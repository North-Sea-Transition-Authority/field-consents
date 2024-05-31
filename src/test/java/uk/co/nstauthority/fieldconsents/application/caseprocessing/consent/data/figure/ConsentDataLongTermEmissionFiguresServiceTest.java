package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

@ExtendWith(MockitoExtension.class)
class ConsentDataLongTermEmissionFiguresServiceTest {

  @Mock
  private ConsentDataLongTermEmissionFiguresRepository consentDataLongTermEmissionFiguresRepository;

  @InjectMocks
  @Spy
  private ConsentDataLongTermEmissionFiguresService consentDataLongTermEmissionFiguresService;

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void getConsentDataLongTermEmissionFiguresList(ApplicationType applicationType) {
    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);

    var consentDataLongTermEmissionFiguresList = List.of(
        ConsentDataLongTermEmissionFiguresTestUtil.builder().build(),
        ConsentDataLongTermEmissionFiguresTestUtil.builder().build()
    );

    when(consentDataLongTermEmissionFiguresRepository.findAllByApplication(application))
        .thenReturn(consentDataLongTermEmissionFiguresList);

    assertThat(consentDataLongTermEmissionFiguresService.getConsentDataLongTermEmissionFiguresList(application))
        .isEqualTo(consentDataLongTermEmissionFiguresList);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void getConsentDataLongTermEmissionFiguresViews(ApplicationType applicationType) {
    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);

    var consentDataLongTermEmissionFigures2024 = ConsentDataLongTermEmissionFiguresTestUtil.builder()
        .withApplication(application)
        .withYear(2024)
        .withDailyAverage(BigDecimal.valueOf(428.76))
        .build();
    var consentDataLongTermEmissionFigures2025 = ConsentDataLongTermEmissionFiguresTestUtil.builder()
        .withApplication(application)
        .withYear(2025)
        .withDailyAverage(BigDecimal.valueOf(583.24))
        .build();
    var consentDataLongTermEmissionFigures2026 = ConsentDataLongTermEmissionFiguresTestUtil.builder()
        .withApplication(application)
        .withYear(2026)
        .withDailyAverage(BigDecimal.valueOf(241.57))
        .build();
    var consentDataLongTermEmissionFigures2027 = ConsentDataLongTermEmissionFiguresTestUtil.builder()
        .withApplication(application)
        .withYear(2027)
        .withDailyAverage(BigDecimal.valueOf(147.83))
        .build();
    var consentDataLongTermEmissionFigures2028 = ConsentDataLongTermEmissionFiguresTestUtil.builder()
        .withApplication(application)
        .withYear(2028)
        .withDailyAverage(BigDecimal.valueOf(689.45))
        .build();

    var consentDataLongTermEmissionFiguresList = List.of(
        consentDataLongTermEmissionFigures2026,
        consentDataLongTermEmissionFigures2028,
        consentDataLongTermEmissionFigures2025,
        consentDataLongTermEmissionFigures2024,
        consentDataLongTermEmissionFigures2027
    );

    doReturn(consentDataLongTermEmissionFiguresList)
        .when(consentDataLongTermEmissionFiguresService)
        .getConsentDataLongTermEmissionFiguresList(application);

    assertThat(consentDataLongTermEmissionFiguresService.getConsentDataLongTermEmissionFiguresViews(application)).containsExactly(
        entry(consentDataLongTermEmissionFigures2024.getYear().toString(),
            bigDecimalToFormattedString(consentDataLongTermEmissionFigures2024.getDailyAverage())),
        entry(consentDataLongTermEmissionFigures2025.getYear().toString(),
            bigDecimalToFormattedString(consentDataLongTermEmissionFigures2025.getDailyAverage())),
        entry(consentDataLongTermEmissionFigures2026.getYear().toString(),
            bigDecimalToFormattedString(consentDataLongTermEmissionFigures2026.getDailyAverage())),
        entry(consentDataLongTermEmissionFigures2027.getYear().toString(),
            bigDecimalToFormattedString(consentDataLongTermEmissionFigures2027.getDailyAverage())),
        entry(consentDataLongTermEmissionFigures2028.getYear().toString(),
            bigDecimalToFormattedString(consentDataLongTermEmissionFigures2028.getDailyAverage()))
    );
  }
}
