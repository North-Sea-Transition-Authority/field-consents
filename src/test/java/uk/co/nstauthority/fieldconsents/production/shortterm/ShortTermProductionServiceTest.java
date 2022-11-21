package uk.co.nstauthority.fieldconsents.production.shortterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.START_DATE;
import static uk.co.nstauthority.fieldconsents.production.ProductionTestUtils.START_MONTH_CONSENT_DAYS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;

@ExtendWith(MockitoExtension.class)
class ShortTermProductionServiceTest {

  @Mock
  private ShortTermProductionMonthRepository shortTermProductionMonthRepository;

  @Mock
  private ProductionRowService productionRowService;

  @Mock
  private ConsentLengthService consentLengthService;

  private ShortTermProductionService shortTermProductionService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthDetails consentLengthDetails;

  @BeforeEach
  void setUp() {
    shortTermProductionService = new ShortTermProductionService(
        productionRowService,
        consentLengthService,
        shortTermProductionMonthRepository
    );
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
  }

  @Test
  void shortTermProductionMonthsExist_false() {
    when(shortTermProductionMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(false);

    assertThat(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).isFalse();
  }

  @Test
  void shortTermProductionMonthsExist_true() {
    when(shortTermProductionMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(true);

    assertThat(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).isTrue();
  }

  @Test
  void shortTermProductionMonthsComplete_falseNoneExist() {
    when(shortTermProductionMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));

    assertThat(shortTermProductionService.shortTermProductionMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void shortTermProductionMonthsComplete_falseProdRowsExist() {
    List<ShortTermProductionMonth> shortTermProductionMonths =
        ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion);
    shortTermProductionMonths.remove(1); // remove a month of data

    when(shortTermProductionMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(shortTermProductionMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    assertThat(shortTermProductionService.shortTermProductionMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void shortTermProductionMonthsComplete_true() {
    when(shortTermProductionMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion));
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    assertThat(shortTermProductionService.shortTermProductionMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getShortTermProductionForm_withInitialForm() {
    when(shortTermProductionMonthRepository.findAllByApplicationVersion(any())).thenReturn(new LinkedList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    ShortTermProductionForm shortTermProductionForm = shortTermProductionService.getShortTermProductionForm(
        applicationVersion
    );

    List<ShortTermProductionMonthForm> shortTermProductionMonthForms = shortTermProductionForm.getShortTermProductionMonthForms();

    // Test individual short term production month form
    ShortTermProductionForm expectedForm = ProductionTestUtils.getEmptyShortTermProductionForm();
    List<ShortTermProductionMonthForm> expectedProductionMonthForms = expectedForm.getShortTermProductionMonthForms();

    for(int index = 0; index < expectedProductionMonthForms.size(); index++) {
      ShortTermProductionMonthForm actualMonthForm = shortTermProductionMonthForms.get(index);
      ShortTermProductionMonthForm expectedMonthForm = expectedProductionMonthForms.get(index);

      DecimalInput oilMinValue = actualMonthForm.getOilMinValue();
      DecimalInput oilMaxValue = actualMonthForm.getOilMaxValue();
      DecimalInput gasMinValue = actualMonthForm.getGasMinValue();
      DecimalInput gasMaxValue = actualMonthForm.getGasMaxValue();

      assertFormDecimalInputs(actualMonthForm, expectedMonthForm, oilMinValue, oilMaxValue, gasMinValue, gasMaxValue);
      assertThat(oilMinValue.getInputValue()).isNull();
      assertThat(oilMaxValue.getInputValue()).isNull();
      assertThat(gasMinValue.getInputValue()).isNull();
      assertThat(gasMaxValue.getInputValue()).isNull();
    }
  }

  private void assertFormDecimalInputs(ShortTermProductionMonthForm monthForm,
                                       ShortTermProductionMonthForm expectedMonthForm, DecimalInput oilMinValue,
                                       DecimalInput oilMaxValue, DecimalInput gasMinValue, DecimalInput gasMaxValue) {
    assertThat(monthForm.getMonth()).isEqualTo(expectedMonthForm.getMonth());
    assertThat(monthForm.getYear()).isEqualTo(expectedMonthForm.getYear());
    assertThat(monthForm.getConsentDays()).isEqualTo(expectedMonthForm.getConsentDays());
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
  void getShortTermProductionForm_withCompleteForm() {
    var shortTermProductionMonths = ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion);
    when(shortTermProductionMonthRepository.findAllByApplicationVersion(any())).thenReturn(shortTermProductionMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    doCallRealMethod().when(productionRowService).populateFormWithPreviousProductionRow(any(ProductionRow.class), any(ProductionRowForm.class));

    ShortTermProductionForm shortTermProductionForm = shortTermProductionService.getShortTermProductionForm(applicationVersion);

    List<ShortTermProductionMonthForm> shortTermProductionMonthForms = shortTermProductionForm.getShortTermProductionMonthForms();

    for(int index = 0; index < shortTermProductionMonths.size(); index ++) {
      ShortTermProductionMonthForm monthForm = shortTermProductionMonthForms.get(index);
      DecimalInput oilMinValue = monthForm.getOilMinValue();
      DecimalInput oilMaxValue = monthForm.getOilMaxValue();
      DecimalInput gasMinValue = monthForm.getGasMinValue();
      DecimalInput gasMaxValue = monthForm.getGasMaxValue();

      assertThat(new BigDecimal(oilMinValue.getInputValue())).isEqualTo(shortTermProductionMonths.get(index).getOilMinValue());
      assertThat(new BigDecimal(oilMaxValue.getInputValue())).isEqualTo(shortTermProductionMonths.get(index).getOilMaxValue());
      assertThat(new BigDecimal(gasMinValue.getInputValue())).isEqualTo(shortTermProductionMonths.get(index).getGasMinValue());
      assertThat(new BigDecimal(gasMaxValue.getInputValue())).isEqualTo(shortTermProductionMonths.get(index).getGasMaxValue());
      index ++;
    }
  }

  @Test
  void saveShortTermProductionMonthDetails() {
    LocalDate lastDayStartDate = LocalDate.of(2022, 10, 31);
    ShortTermProductionMonth shortTermProductionMonth = ProductionTestUtils.getShortTermProductionMonth(
        applicationVersion,
        1,
        START_DATE.getMonth(),
        START_DATE.getYear(),
        START_DATE,
        lastDayStartDate
    );
    ShortTermProductionMonthForm shortTermProductionMonthForm = ProductionTestUtils.getCompleteShortTermProductionMonthForm(START_DATE, START_MONTH_CONSENT_DAYS);
    doCallRealMethod().when(productionRowService).updateProductionRowFromForm(any(ProductionRowForm.class), any(ProductionRow.class));

    shortTermProductionService.saveShortTermProductionMonthDetails(applicationVersion, shortTermProductionMonthForm);

    ArgumentCaptor<ShortTermProductionMonth> productionMonthArgumentCaptor = ArgumentCaptor.forClass(ShortTermProductionMonth.class);
    verify(shortTermProductionMonthRepository, times(1)).save(productionMonthArgumentCaptor.capture());

    ShortTermProductionMonth expectedProductionMonth = productionMonthArgumentCaptor.getValue();

    assertThat(expectedProductionMonth.getMonth()).isEqualTo(shortTermProductionMonth.getMonth());
    assertThat(expectedProductionMonth.getYear()).isEqualTo(shortTermProductionMonth.getYear());
    assertThat(expectedProductionMonth.getStartDate()).isEqualTo(shortTermProductionMonth.getStartDate());
    assertThat(expectedProductionMonth.getEndDate()).isEqualTo(shortTermProductionMonth.getEndDate());
    ApplicationVersion expectedApplicationVersion = expectedProductionMonth.getApplicationVersion();
    assertThat(expectedApplicationVersion.getId()).isEqualTo(applicationVersion.getId());
    assertThat(expectedApplicationVersion.getVersion()).isEqualTo(applicationVersion.getVersion());
    assertThat(expectedApplicationVersion.getApplication()).isEqualTo(applicationVersion.getApplication());
    assertThat(expectedProductionMonth.getOilMinValue()).isEqualTo(shortTermProductionMonth.getOilMinValue());
    assertThat(expectedProductionMonth.getOilMinUnit()).isEqualTo(shortTermProductionMonth.getOilMinUnit());
    assertThat(expectedProductionMonth.getOilMaxValue()).isEqualTo(shortTermProductionMonth.getOilMaxValue());
    assertThat(expectedProductionMonth.getOilMaxUnit()).isEqualTo(shortTermProductionMonth.getOilMaxUnit());
    assertThat(expectedProductionMonth.getGasMinValue()).isEqualTo(shortTermProductionMonth.getGasMinValue());
    assertThat(expectedProductionMonth.getGasMinUnit()).isEqualTo(shortTermProductionMonth.getGasMinUnit());
    assertThat(expectedProductionMonth.getGasMaxValue()).isEqualTo(shortTermProductionMonth.getGasMaxValue());
    assertThat(expectedProductionMonth.getGasMaxUnit()).isEqualTo(shortTermProductionMonth.getGasMaxUnit());
  }
}