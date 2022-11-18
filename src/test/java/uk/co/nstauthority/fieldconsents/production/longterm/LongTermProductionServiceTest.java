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
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

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
            LongTermProductionYearForm::getOilMinUnit,
            yearForm -> yearForm.getOilMaxValue().getAsBigDecimal(),
            yearForm -> yearForm.getOilMaxValue().getFieldName(),
            yearForm -> yearForm.getOilMaxValue().getDisplayName(),
            LongTermProductionYearForm::getOilMaxUnit,
            yearForm -> yearForm.getGasMinValue().getAsBigDecimal(),
            yearForm -> yearForm.getGasMinValue().getFieldName(),
            yearForm -> yearForm.getGasMinValue().getDisplayName(),
            LongTermProductionYearForm::getGasMinUnit,
            yearForm -> yearForm.getGasMaxValue().getAsBigDecimal(),
            yearForm -> yearForm.getGasMaxValue().getFieldName(),
            yearForm -> yearForm.getGasMaxValue().getDisplayName(),
            LongTermProductionYearForm::getGasMaxUnit
        )
        .containsExactly(
            tuple(START_YEAR_LT.toString(),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName, ProductionUnit.KSCM_PER_DAY,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName, ProductionUnit.KSCM_PER_DAY),
            tuple(String.valueOf(START_YEAR_LT + 1),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName, ProductionUnit.KSCM_PER_DAY,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName, ProductionUnit.KSCM_PER_DAY),
            tuple(String.valueOf(START_YEAR_LT + 2),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName, ProductionUnit.KSCM_PER_DAY,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName, ProductionUnit.KSCM_PER_DAY),
            tuple(String.valueOf(START_YEAR_LT + 3),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName, ProductionUnit.KSCM_PER_DAY,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName, ProductionUnit.KSCM_PER_DAY),
            tuple(END_YEAR_LT.toString(),
                Optional.empty(), oilMinValueFieldName, oilMinValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), oilMaxValueFieldName, oilMaxValueDisplayName, ProductionUnit.SCM_PER_DAY,
                Optional.empty(), gasMinValueFieldName, gasMinValueDisplayName, ProductionUnit.KSCM_PER_DAY,
                Optional.empty(), gasMaxValueFieldName, gasMaxValueDisplayName, ProductionUnit.KSCM_PER_DAY)
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
            LongTermProductionYearForm::getOilMinUnit,
            yearForm -> yearForm.getOilMaxValue().getAsBigDecimal().get(),
            LongTermProductionYearForm::getOilMaxUnit,
            yearForm -> yearForm.getGasMinValue().getAsBigDecimal().get(),
            LongTermProductionYearForm::getGasMinUnit,
            yearForm -> yearForm.getGasMaxValue().getAsBigDecimal().get(),
            LongTermProductionYearForm::getGasMaxUnit
        )
        .containsExactly(
            tuple(START_YEAR_LT.toString(),
                longTermProductionYears.get(0).getOilMinValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(0).getOilMaxValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(0).getGasMinValue(), ProductionUnit.KSCM_PER_DAY,
                longTermProductionYears.get(0).getGasMaxValue(), ProductionUnit.KSCM_PER_DAY),
            tuple(String.valueOf(START_YEAR_LT + 1),
                longTermProductionYears.get(1).getOilMinValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(1).getOilMaxValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(1).getGasMinValue(), ProductionUnit.KSCM_PER_DAY,
                longTermProductionYears.get(1).getGasMaxValue(), ProductionUnit.KSCM_PER_DAY),
            tuple(String.valueOf(START_YEAR_LT + 2),
                longTermProductionYears.get(2).getOilMinValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(2).getOilMaxValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(2).getGasMinValue(), ProductionUnit.KSCM_PER_DAY,
                longTermProductionYears.get(2).getGasMaxValue(), ProductionUnit.KSCM_PER_DAY),
            tuple(String.valueOf(START_YEAR_LT + 3),
                longTermProductionYears.get(3).getOilMinValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(3).getOilMaxValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(3).getGasMinValue(), ProductionUnit.KSCM_PER_DAY,
                longTermProductionYears.get(3).getGasMaxValue(), ProductionUnit.KSCM_PER_DAY),
            tuple(END_YEAR_LT.toString(),
                longTermProductionYears.get(4).getOilMinValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(4).getOilMaxValue(), ProductionUnit.SCM_PER_DAY,
                longTermProductionYears.get(4).getGasMinValue(), ProductionUnit.KSCM_PER_DAY,
                longTermProductionYears.get(4).getGasMaxValue(), ProductionUnit.KSCM_PER_DAY)
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
    assertThat(expectedProductionYear.getOilMinUnit()).isEqualTo(longTermProductionYear.getOilMinUnit());
    assertThat(expectedProductionYear.getOilMaxValue()).isEqualTo(longTermProductionYear.getOilMaxValue());
    assertThat(expectedProductionYear.getOilMaxUnit()).isEqualTo(longTermProductionYear.getOilMaxUnit());
    assertThat(expectedProductionYear.getGasMinValue()).isEqualTo(longTermProductionYear.getGasMinValue());
    assertThat(expectedProductionYear.getGasMinUnit()).isEqualTo(longTermProductionYear.getGasMinUnit());
    assertThat(expectedProductionYear.getGasMaxValue()).isEqualTo(longTermProductionYear.getGasMaxValue());
    assertThat(expectedProductionYear.getGasMaxUnit()).isEqualTo(longTermProductionYear.getGasMaxUnit());
  }

}