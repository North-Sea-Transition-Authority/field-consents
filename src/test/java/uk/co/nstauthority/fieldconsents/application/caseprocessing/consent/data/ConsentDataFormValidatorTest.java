package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresInput;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresInputValidator;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class ConsentDataFormValidatorTest {

  @Mock
  private ConsentProductionFiguresInputValidator consentProductionFiguresInputValidator;

  @InjectMocks
  private ConsentDataFormValidator validator;

  private ConsentDataForm form;
  private BindingResult bindingResult;

  @BeforeEach
  void setUp() {
    form = new ConsentDataForm();
    bindingResult = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void validate() {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_sameDay() {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2024-01-01"));

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_noConsentStartDate_noConsentEndDate() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .map(FieldError::getField)
        .allSatisfy(code -> assertTrue(code.startsWith("consentStartDate")));
  }

  @Test
  void validate_noConsentEndDate() {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .map(FieldError::getField)
        .allSatisfy(code -> assertTrue(code.startsWith("consentEndDate")));
  }

  @Test
  void validate_consentEndDateNotAfterConsentStartDate() {
    var date = LocalDate.parse("2024-01-01");

    form.getConsentStartDateInput().setDate(date);
    form.getConsentEndDateInput().setDate(date);

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .map(FieldError::getField)
        .allSatisfy(code -> assertTrue(code.startsWith("consentEndDate")));
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsProductionAndConsentLengthTypeIsShortTermOrAnnual(ConsentLengthType consentLengthType) {
    var shortTermOrAnnualConsentProductionFiguresInput = mock(ConsentProductionFiguresInput.class);

    form.setShortTermOrAnnualConsentProductionFiguresInput(shortTermOrAnnualConsentProductionFiguresInput);

    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    verify(consentProductionFiguresInputValidator).validate(shortTermOrAnnualConsentProductionFiguresInput, bindingResult);
  }

  @Test
  void validate_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm_noLongTermProductionConsentScheduleStartDate() {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getLongTermProductionConsentScheduleStartDateInput().setDate(null);

    var longTermConsentProductionFiguresInput2024 = mock(ConsentProductionFiguresInput.class);
    var longTermConsentProductionFiguresInput2025 = mock(ConsentProductionFiguresInput.class);

    var longTermConsentProductionFiguresInputs = Map.of(
        "2024", longTermConsentProductionFiguresInput2024,
        "2025", longTermConsentProductionFiguresInput2025
    );

    form.setLongTermConsentProductionFiguresInputs(longTermConsentProductionFiguresInputs);

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.LONG_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    verify(consentProductionFiguresInputValidator).validate(longTermConsentProductionFiguresInput2024, bindingResult);
    verify(consentProductionFiguresInputValidator).validate(longTermConsentProductionFiguresInput2025, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry(
            "longTermProductionConsentScheduleStartDateInput.dayInput.inputValue",
            List.of("Consent schedule start date must be a real date")
        ),
        entry(
            "longTermProductionConsentScheduleStartDateInput.monthInput.inputValue",
            List.of("")
        ),
        entry(
            "longTermProductionConsentScheduleStartDateInput.yearInput.inputValue",
            List.of("")
        )
    );
  }

  @Test
  void validate_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm_longTermProductionConsentScheduleStartDateBeforeConsentStartDate() {
    var consentStartDate = LocalDate.parse("2024-01-01");

    form.getConsentStartDateInput().setDate(consentStartDate);
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getLongTermProductionConsentScheduleStartDateInput().setDate(consentStartDate.minusDays(1));

    var longTermConsentProductionFiguresInput2024 = mock(ConsentProductionFiguresInput.class);
    var longTermConsentProductionFiguresInput2025 = mock(ConsentProductionFiguresInput.class);

    var longTermConsentProductionFiguresInputs = Map.of(
        "2024", longTermConsentProductionFiguresInput2024,
        "2025", longTermConsentProductionFiguresInput2025
    );

    form.setLongTermConsentProductionFiguresInputs(longTermConsentProductionFiguresInputs);

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.LONG_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    verify(consentProductionFiguresInputValidator).validate(longTermConsentProductionFiguresInput2024, bindingResult);
    verify(consentProductionFiguresInputValidator).validate(longTermConsentProductionFiguresInput2025, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry(
            "longTermProductionConsentScheduleStartDateInput.dayInput.inputValue",
            List.of("Consent schedule start date must be on or after the consent start date")
        ),
        entry(
            "longTermProductionConsentScheduleStartDateInput.monthInput.inputValue",
            List.of("")
        ),
        entry(
            "longTermProductionConsentScheduleStartDateInput.yearInput.inputValue",
            List.of("")
        )
    );
  }

  @Test
  void validate_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm_longTermProductionConsentScheduleStartDateAfterConsentEndDate() {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));

    var consentEndDate = LocalDate.parse("2025-01-01");
    form.getConsentEndDateInput().setDate(consentEndDate);

    form.getLongTermProductionConsentScheduleStartDateInput().setDate(consentEndDate.plusDays(1));

    var longTermConsentProductionFiguresInput2024 = mock(ConsentProductionFiguresInput.class);
    var longTermConsentProductionFiguresInput2025 = mock(ConsentProductionFiguresInput.class);

    var longTermConsentProductionFiguresInputs = Map.of(
        "2024", longTermConsentProductionFiguresInput2024,
        "2025", longTermConsentProductionFiguresInput2025
    );

    form.setLongTermConsentProductionFiguresInputs(longTermConsentProductionFiguresInputs);

    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentLengthType = ConsentLengthType.LONG_TERM;

    when(consentProductionFiguresInputValidator.supports(ConsentProductionFiguresInput.class)).thenReturn(true);

    validator.validate(form, application, consentLengthType, bindingResult);

    verify(consentProductionFiguresInputValidator).validate(longTermConsentProductionFiguresInput2024, bindingResult);
    verify(consentProductionFiguresInputValidator).validate(longTermConsentProductionFiguresInput2025, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry(
            "longTermProductionConsentScheduleStartDateInput.dayInput.inputValue",
            List.of("Consent schedule start date must be on or before the consent end date")
        ),
        entry(
            "longTermProductionConsentScheduleStartDateInput.monthInput.inputValue",
            List.of("")
        ),
        entry(
            "longTermProductionConsentScheduleStartDateInput.yearInput.inputValue",
            List.of("")
        )
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsFlareOrVent_emissionDailyAverageValid(ApplicationType applicationType) {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getEmissionDailyAverageInput().setInputValue("1");

    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    validator.validate(form, application, consentLengthType, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsFlareOrVent_emissionDailyAverageEmpty(ApplicationType applicationType) {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getEmissionDailyAverageInput().setInputValue(null);

    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    validator.validate(form, application, consentLengthType, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry("emissionDailyAverageInput.inputValue", Collections.singletonList("Enter daily average"))
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsFlareOrVent_emissionDailyAverageNonNumerical(ApplicationType applicationType) {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getEmissionDailyAverageInput().setInputValue("test");

    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    validator.validate(form, application, consentLengthType, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry("emissionDailyAverageInput.inputValue", Collections.singletonList("Daily average must be a number"))
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsFlareOrVent_emissionDailyAverageNegative(ApplicationType applicationType) {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getEmissionDailyAverageInput().setInputValue("-0.5");

    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    validator.validate(form, application, consentLengthType, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry("emissionDailyAverageInput.inputValue", Collections.singletonList("Daily average must be 0 or more"))
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsFlareOrVent_emissionDailyAverageHasTooManyDecimalPlaces(ApplicationType applicationType) {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getEmissionDailyAverageInput().setInputValue("1.1234567");

    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    validator.validate(form, application, consentLengthType, bindingResult);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry("emissionDailyAverageInput.inputValue", Collections.singletonList("Daily average must include no more than 6 decimal places"))
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void validate_applicationTypeIsFlareOrVent_emissionDailyAverageHasMaxDecimalPlaces(ApplicationType applicationType) {
    form.getConsentStartDateInput().setDate(LocalDate.parse("2024-01-01"));
    form.getConsentEndDateInput().setDate(LocalDate.parse("2025-01-01"));

    form.getEmissionDailyAverageInput().setInputValue("1.123456");

    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    validator.validate(form, application, consentLengthType, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }
}
