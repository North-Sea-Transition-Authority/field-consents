package uk.co.nstauthority.fieldconsents.production.annual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.PRODUCTION_YEAR;

import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;

@ExtendWith(MockitoExtension.class)
class AnnualProductionServiceTest {

  @Mock
  private AnnualProductionMonthRepository annualProductionMonthRepository;

  @Mock
  private ProductionRowService productionRowService;

  private AnnualProductionService annualProductionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    annualProductionService = new AnnualProductionService(productionRowService, annualProductionMonthRepository);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getAnnualProductionForm_withInitialForm() {

    when(annualProductionMonthRepository.findAllByApplicationVersion(any())).thenReturn(new LinkedList<>());
    AnnualProductionForm annualProductionForm = annualProductionService.getAnnualProductionForm(applicationVersion, PRODUCTION_YEAR);

    var allMonths = Month.values();
    List<AnnualProductionMonthForm> annualProductionMonthForms = annualProductionForm.getAnnualProductionMonthForms();
    assertThat(annualProductionForm.getYear()).isEqualTo(PRODUCTION_YEAR);

    for(int index = 0; index < annualProductionMonthForms.size(); index++) {
      AnnualProductionMonthForm monthForm = annualProductionMonthForms.get(index);
      DecimalInput oilMinValue = monthForm.getOilMinValue();
      DecimalInput oilMaxValue = monthForm.getOilMaxValue();
      DecimalInput gasMinValue = monthForm.getGasMinValue();
      DecimalInput gasMaxValue = monthForm.getGasMaxValue();

      assertFormDecimalInputs(monthForm, oilMinValue, oilMaxValue, gasMinValue, gasMaxValue, allMonths[index]);
      assertThat(oilMinValue.getInputValue()).isNull();
      assertThat(oilMaxValue.getInputValue()).isNull();
      assertThat(gasMinValue.getInputValue()).isNull();
      assertThat(gasMaxValue.getInputValue()).isNull();
    }
  }

  /**
   * This is only used once to test that the initial form is created with the correct DecimalInput details.
   */
  private void assertFormDecimalInputs(AnnualProductionMonthForm monthForm, DecimalInput oilMinValue, DecimalInput oilMaxValue,
                                       DecimalInput gasMinValue, DecimalInput gasMaxValue, Month month) {
    assertThat(monthForm.getMonth()).isEqualTo(Character.toUpperCase(month.name().charAt(0)) + month.name().substring(1).toLowerCase());
    assertThat(oilMinValue.getFieldName()).isEqualTo("oilMinValue");
    assertThat(oilMinValue.getDisplayName()).isEqualTo("Minimum oil");
    assertThat(oilMaxValue.getFieldName()).isEqualTo("oilMaxValue");
    assertThat(oilMaxValue.getDisplayName()).isEqualTo("Maximum oil");
    assertThat(gasMinValue.getFieldName()).isEqualTo("gasMinValue");
    assertThat(gasMinValue.getDisplayName()).isEqualTo("Minimum gas");
    assertThat(gasMaxValue.getFieldName()).isEqualTo("gasMaxValue");
    assertThat(gasMaxValue.getDisplayName()).isEqualTo("Maximum gas");
  }

  @Test
  void getAnnualProductionForm_withCompleteForm() {
    List<AnnualProductionMonth> annualProductionMonths = ProductionTestUtils.getAnnualProductionMonthsData(new ApplicationVersion());
    when(annualProductionMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(annualProductionMonths);
    doCallRealMethod().when(productionRowService).populateFormWithPreviousProductionRow(any(ProductionRow.class), any(ProductionRowForm.class));

    AnnualProductionForm annualProductionForm = annualProductionService.getAnnualProductionForm(applicationVersion, PRODUCTION_YEAR);

    List<AnnualProductionMonthForm> annualProductionMonthForms = annualProductionForm.getAnnualProductionMonthForms();
    assertThat(annualProductionForm.getYear()).isEqualTo(PRODUCTION_YEAR);

    for(int index = 0; index < annualProductionMonthForms.size(); index++) {
      AnnualProductionMonthForm monthForm = annualProductionMonthForms.get(index);
      DecimalInput oilMinValue = monthForm.getOilMinValue();
      DecimalInput oilMaxValue = monthForm.getOilMaxValue();
      DecimalInput gasMinValue = monthForm.getGasMinValue();
      DecimalInput gasMaxValue = monthForm.getGasMaxValue();

      assertThat(oilMinValue.getInputValueAsBigDecimal().get()).isEqualTo(annualProductionMonths.get(index).getOilMinValue());
      assertThat(oilMaxValue.getInputValueAsBigDecimal().get()).isEqualTo(annualProductionMonths.get(index).getOilMaxValue());
      assertThat(gasMinValue.getInputValueAsBigDecimal().get()).isEqualTo(annualProductionMonths.get(index).getGasMinValue());
      assertThat(gasMaxValue.getInputValueAsBigDecimal().get()).isEqualTo(annualProductionMonths.get(index).getGasMaxValue());
    }
  }

  @Test
  void createAnnualProductionMonthDetails() {
    Month productionMonth = Month.OCTOBER;
    AnnualProductionMonth annualProductionMonth = ProductionTestUtils.getAnnualProductionMonth(applicationVersion, 10, productionMonth);
    AnnualProductionForm annualProductionForm = ProductionTestUtils.getCompleteAnnualProductionForm();

    doCallRealMethod().when(productionRowService).updateProductionRowFromForm(any(ProductionRowForm.class), any(ProductionRow.class));

    annualProductionService.saveAnnualProductionMonthDetails(
        applicationVersion,
        annualProductionForm.getAnnualProductionMonthForms().get(productionMonth.getValue() - 1),
        PRODUCTION_YEAR);

    ArgumentCaptor<AnnualProductionMonth> productionMonthArgumentCaptor = ArgumentCaptor.forClass(AnnualProductionMonth.class);
    verify(annualProductionMonthRepository, times(1)).save(productionMonthArgumentCaptor.capture());

    AnnualProductionMonth expectedProductionMonth = productionMonthArgumentCaptor.getValue();

    assertThat(expectedProductionMonth.getMonth()).isEqualTo(annualProductionMonth.getMonth());
    assertThat(expectedProductionMonth.getYear()).isEqualTo(Integer.parseInt(PRODUCTION_YEAR));
    ApplicationVersion expectedApplicationVersion = expectedProductionMonth.getApplicationVersion();
    assertThat(expectedApplicationVersion.getId()).isEqualTo(applicationVersion.getId());
    assertThat(expectedApplicationVersion.getVersion()).isEqualTo(applicationVersion.getVersion());
    assertThat(expectedApplicationVersion.getApplication()).isEqualTo(applicationVersion.getApplication());
    assertThat(expectedProductionMonth.getOilMinValue()).isEqualTo(annualProductionMonth.getOilMinValue());
    assertThat(expectedProductionMonth.getOilMinUnit()).isEqualTo(annualProductionMonth.getOilMinUnit());
    assertThat(expectedProductionMonth.getOilMaxValue()).isEqualTo(annualProductionMonth.getOilMaxValue());
    assertThat(expectedProductionMonth.getOilMaxUnit()).isEqualTo(annualProductionMonth.getOilMaxUnit());
    assertThat(expectedProductionMonth.getGasMinValue()).isEqualTo(annualProductionMonth.getGasMinValue());
    assertThat(expectedProductionMonth.getGasMinUnit()).isEqualTo(annualProductionMonth.getGasMinUnit());
    assertThat(expectedProductionMonth.getGasMaxValue()).isEqualTo(annualProductionMonth.getGasMaxValue());
    assertThat(expectedProductionMonth.getGasMaxUnit()).isEqualTo(annualProductionMonth.getGasMaxUnit());
  }
}