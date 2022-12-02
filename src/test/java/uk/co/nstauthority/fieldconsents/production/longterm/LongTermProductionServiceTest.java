package uk.co.nstauthority.fieldconsents.production.longterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.END_YEAR_LT;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.START_YEAR_LT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;

@ExtendWith(MockitoExtension.class)
class LongTermProductionServiceTest {

  @Mock
  private LongTermProductionYearRepository longTermProductionYearRepository;

  @Mock
  private ProductionRowService productionRowService;

  @Mock
  private ConsentLengthService consentLengthService;

  private LongTermProductionService longTermProductionService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthDetails consentLengthDetails;

  @BeforeEach
  void setUp() {
    longTermProductionService = new LongTermProductionService(
        productionRowService,
        consentLengthService,
        longTermProductionYearRepository
    );
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
  }

  @Test
  void longTermProductionYearsExist_false() {
    when(longTermProductionYearRepository.existsByApplicationVersion(applicationVersion)).thenReturn(false);

    assertThat(longTermProductionService.longTermProductionYearsExist(applicationVersion)).isFalse();
  }

  @Test
  void longTermProductionYearsExist_true() {
    when(longTermProductionYearRepository.existsByApplicationVersion(applicationVersion)).thenReturn(true);

    assertThat(longTermProductionService.longTermProductionYearsExist(applicationVersion)).isTrue();
  }

  @Test
  void longTermProductionYearsComplete_falseNoneExist() {
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion))
        .thenReturn(new ArrayList<>());
    // 2022 to 2026
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(longTermProductionService.longTermProductionYearsComplete(applicationVersion)).isFalse();
  }

  @Test
  void longTermProductionYearsComplete_falseProdRowsExistWrongYears() {
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion))
        .thenReturn(ProductionTestUtils.getLongTermProductionYearsData(applicationVersion)); // 2022 to 2026
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion, 2021, 2024));

    assertThat(longTermProductionService.longTermProductionYearsComplete(applicationVersion)).isFalse();
  }

  @Test
  void longTermProductionYearsComplete_falseProdRowsExistWrongYearsNotOverlapping() {
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion))
        .thenReturn(ProductionTestUtils.getLongTermProductionYearsData(applicationVersion)); // 2022 to 2026
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion, 2020, 2021));

    assertThat(longTermProductionService.longTermProductionYearsComplete(applicationVersion)).isFalse();
  }

  @Test
  void longTermProductionYearsComplete_true() {
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion))
        .thenReturn(ProductionTestUtils.getLongTermProductionYearsData(applicationVersion)); // 2022 to 2026
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails); // 2022 to 2026

    assertThat(longTermProductionService.longTermProductionYearsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getLongTermProductionForm_initialStubForm() {
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion))
        .thenReturn(new ArrayList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    LongTermProductionForm longTermProductionForm = longTermProductionService.getLongTermProductionForm(applicationVersion);

    var yearForms = longTermProductionForm.getLongTermProductionYearForms();

    String oilMinValueFieldName = "oilMinValue";
    String oilMinValueDisplayName = "Minimum oil";
    String oilMaxValueFieldName = "oilMaxValue";
    String oilMaxValueDisplayName = "Maximum oil";
    String gasMinValueFieldName = "gasMinValue";
    String gasMinValueDisplayName = "Minimum gas";
    String gasMaxValueFieldName = "gasMaxValue";
    String gasMaxValueDisplayName = "Maximum gas";

    assertThat(yearForms)
        .extracting(
            LongTermProductionYearForm::getYear,
            yearForm -> yearForm.getOilMinValue().getAsBigDecimal(),
            yearForm -> yearForm.getOilMinValue().getFieldName(),
            yearForm -> yearForm.getOilMinValue().getDisplayName(),
            yearForm -> yearForm.getOilMaxValue().getAsBigDecimal(),
            yearForm -> yearForm.getOilMaxValue().getFieldName(),
            yearForm -> yearForm.getOilMaxValue().getDisplayName(),
            yearForm -> yearForm.getGasMinValue().getAsBigDecimal(),
            yearForm -> yearForm.getGasMinValue().getFieldName(),
            yearForm -> yearForm.getGasMinValue().getDisplayName(),
            yearForm -> yearForm.getGasMaxValue().getAsBigDecimal(),
            yearForm -> yearForm.getGasMaxValue().getFieldName(),
            yearForm -> yearForm.getGasMaxValue().getDisplayName()
        )
        .containsExactly(
            tuple(START_YEAR_LT.toString(),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName),
            tuple(String.valueOf(START_YEAR_LT + 1),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName),
            tuple(String.valueOf(START_YEAR_LT + 2),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName),
            tuple(String.valueOf(START_YEAR_LT + 3),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName),
            tuple(END_YEAR_LT.toString(),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName)
        );

  }

  @Test
  void getLongTermProductionForm_completeForm() {
    List<LongTermProductionYear> longTermProductionYears =
        ProductionTestUtils.getLongTermProductionYearsData(applicationVersion);
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion))
        .thenReturn(longTermProductionYears);
    doCallRealMethod().when(productionRowService).populateFormWithPreviousProductionRow(any(ProductionRow.class),
        any(ProductionRowForm.class));
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    LongTermProductionForm longTermProductionForm =
        longTermProductionService.getLongTermProductionForm(applicationVersion);

    var yearForms = longTermProductionForm.getLongTermProductionYearForms();

    assertThat(yearForms)
        .extracting(
            LongTermProductionYearForm::getYear,
            yearForm -> yearForm.getOilMinValue().getAsBigDecimal().get(),
            yearForm -> yearForm.getOilMaxValue().getAsBigDecimal().get(),
            yearForm -> yearForm.getGasMinValue().getAsBigDecimal().get(),
            yearForm -> yearForm.getGasMaxValue().getAsBigDecimal().get()
        )
        .containsExactly(
            tuple(START_YEAR_LT.toString(),
                longTermProductionYears.get(0).getOilMinValue(),
                longTermProductionYears.get(0).getOilMaxValue(),
                longTermProductionYears.get(0).getGasMinValue(),
                longTermProductionYears.get(0).getGasMaxValue()),
            tuple(String.valueOf(START_YEAR_LT + 1),
                longTermProductionYears.get(1).getOilMinValue(),
                longTermProductionYears.get(1).getOilMaxValue(),
                longTermProductionYears.get(1).getGasMinValue(),
                longTermProductionYears.get(1).getGasMaxValue()),
            tuple(String.valueOf(START_YEAR_LT + 2),
                longTermProductionYears.get(2).getOilMinValue(),
                longTermProductionYears.get(2).getOilMaxValue(),
                longTermProductionYears.get(2).getGasMinValue(),
                longTermProductionYears.get(2).getGasMaxValue()),
            tuple(String.valueOf(START_YEAR_LT + 3),
                longTermProductionYears.get(3).getOilMinValue(),
                longTermProductionYears.get(3).getOilMaxValue(),
                longTermProductionYears.get(3).getGasMinValue(),
                longTermProductionYears.get(3).getGasMaxValue()),
            tuple(END_YEAR_LT.toString(),
                longTermProductionYears.get(4).getOilMinValue(),
                longTermProductionYears.get(4).getOilMaxValue(),
                longTermProductionYears.get(4).getGasMinValue(),
                longTermProductionYears.get(4).getGasMaxValue())
        );

  }

  @Test
  void saveLongTermProductionYearDetails() {
    Integer productionYear = 2023;
    LongTermProductionYear longTermProductionYear = ProductionTestUtils.getLongTermProductionYear(applicationVersion, 1, productionYear);
    LongTermProductionYearForm longTermProductionYearForm =
        ProductionTestUtils.getCompleteLongTermProductionYearForm(productionYear);

    doCallRealMethod().when(productionRowService).updateProductionRowFromForm(any(ProductionRowForm.class), any(ProductionRow.class));

    longTermProductionService.saveLongTermProductionYearDetails(applicationVersion,
        longTermProductionYearForm);

    ArgumentCaptor<LongTermProductionYear> productionYearArgumentCaptor = ArgumentCaptor.forClass(LongTermProductionYear.class);
    verify(longTermProductionYearRepository, times(1)).save(productionYearArgumentCaptor.capture());

    LongTermProductionYear expectedProductionYear = productionYearArgumentCaptor.getValue();

    assertThat(expectedProductionYear.getYear()).isEqualTo(longTermProductionYear.getYear());
    assertThat(expectedProductionYear.getApplicationVersion()).isEqualTo(applicationVersion);
    assertThat(expectedProductionYear.getOilMinValue()).isEqualTo(longTermProductionYear.getOilMinValue());
    assertThat(expectedProductionYear.getOilMaxValue()).isEqualTo(longTermProductionYear.getOilMaxValue());
    assertThat(expectedProductionYear.getGasMinValue()).isEqualTo(longTermProductionYear.getGasMinValue());
    assertThat(expectedProductionYear.getGasMaxValue()).isEqualTo(longTermProductionYear.getGasMaxValue());
  }

}