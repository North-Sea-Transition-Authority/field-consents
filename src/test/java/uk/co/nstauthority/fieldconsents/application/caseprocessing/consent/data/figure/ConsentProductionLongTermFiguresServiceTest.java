package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataForm;

@ExtendWith(MockitoExtension.class)
class ConsentProductionLongTermFiguresServiceTest {

  @Mock
  private ConsentProductionLongTermFiguresRepository consentProductionLongTermFiguresRepository;

  @InjectMocks
  @Spy
  private ConsentProductionLongTermFiguresService consentProductionLongTermFiguresService;

  @Captor
  private ArgumentCaptor<List<ConsentProductionLongTermFigures>> consentProductionLongTermFiguresListCaptor;

  @Test
  void getConsentProductionLongTermFiguresList() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var consentProductionLongTermFiguresList = List.of(
        ConsentProductionLongTermFiguresTestUtil.builder().build(),
        ConsentProductionLongTermFiguresTestUtil.builder().build()
    );

    when(consentProductionLongTermFiguresRepository.findAllByApplication(application))
        .thenReturn(consentProductionLongTermFiguresList);

    assertThat(consentProductionLongTermFiguresService.getConsentProductionLongTermFiguresList(application))
        .isEqualTo(consentProductionLongTermFiguresList);
  }

  @Test
  void getConsentProductionLongTermFiguresViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

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

    doReturn(consentProductionLongTermFiguresList)
        .when(consentProductionLongTermFiguresService)
        .getConsentProductionLongTermFiguresList(application);

    assertThat(consentProductionLongTermFiguresService.getConsentProductionLongTermFiguresViews(application)).containsExactly(
        entry("2024", ConsentProductionFiguresView.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2024)),
        entry("2025", ConsentProductionFiguresView.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2025)),
        entry("2026", ConsentProductionFiguresView.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2026)),
        entry("2027", ConsentProductionFiguresView.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2027)),
        entry("2028", ConsentProductionFiguresView.fromConsentProductionLongTermFigures(consentProductionLongTermFigures2028))
    );
  }

  @Test
  void saveConsentProductionLongTermFigures() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);;
    var form = new ConsentDataForm();

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

    form.getLongTermConsentProductionFiguresInputs().putAll(
        Map.of(
            "2024", ConsentProductionFiguresInput.withDefaultValuesFromDto(consentProductionFiguresDto2024),
            "2025", ConsentProductionFiguresInput.withDefaultValuesFromDto(consentProductionFiguresDto2025),
            "2026", ConsentProductionFiguresInput.withDefaultValuesFromDto(consentProductionFiguresDto2026),
            "2027", ConsentProductionFiguresInput.withDefaultValuesFromDto(consentProductionFiguresDto2027),
            "2028", ConsentProductionFiguresInput.withDefaultValuesFromDto(consentProductionFiguresDto2028)
        )
    );

    consentProductionLongTermFiguresService.saveConsentProductionLongTermFigures(application, form);

    verify(consentProductionLongTermFiguresRepository).deleteAllByApplication(application);

    verify(consentProductionLongTermFiguresRepository).saveAll(consentProductionLongTermFiguresListCaptor.capture());

    assertThat(consentProductionLongTermFiguresListCaptor.getValue())
        .isNotNull()
        .extracting(
            ConsentProductionLongTermFigures::getId,
            ConsentProductionLongTermFigures::getYear,
            ConsentProductionLongTermFigures::getMinOil,
            ConsentProductionLongTermFigures::getMaxOil,
            ConsentProductionLongTermFigures::getMinGas,
            ConsentProductionLongTermFigures::getMaxGas
        )
        .containsExactlyInAnyOrder(
            tuple(
                null,
                2024,
                consentProductionFiguresDto2024.minOil(),
                consentProductionFiguresDto2024.maxOil(),
                consentProductionFiguresDto2024.minGas(),
                consentProductionFiguresDto2024.maxGas()
            ),
            tuple(
                null,
                2025,
                consentProductionFiguresDto2025.minOil(),
                consentProductionFiguresDto2025.maxOil(),
                consentProductionFiguresDto2025.minGas(),
                consentProductionFiguresDto2025.maxGas()
            ),
            tuple(
                null,
                2026,
                consentProductionFiguresDto2026.minOil(),
                consentProductionFiguresDto2026.maxOil(),
                consentProductionFiguresDto2026.minGas(),
                consentProductionFiguresDto2026.maxGas()
            ),
            tuple(
                null,
                2027,
                consentProductionFiguresDto2027.minOil(),
                consentProductionFiguresDto2027.maxOil(),
                consentProductionFiguresDto2027.minGas(),
                consentProductionFiguresDto2027.maxGas()
            ),
            tuple(
                null,
                2028,
                consentProductionFiguresDto2028.minOil(),
                consentProductionFiguresDto2028.maxOil(),
                consentProductionFiguresDto2028.minGas(),
                consentProductionFiguresDto2028.maxGas()
            )
        );
  }

  @Test
  void deleteConsentProductionLongTermFigures() {
    var application = new Application();
    consentProductionLongTermFiguresService.deleteConsentProductionLongTermFigures(application);
    verify(consentProductionLongTermFiguresRepository).deleteAllByApplication(application);
  }
}
