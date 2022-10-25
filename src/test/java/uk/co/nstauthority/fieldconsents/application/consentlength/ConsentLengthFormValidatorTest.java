package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class ConsentLengthFormValidatorTest {

  private static final String CONSENT_LENGTH_PERIOD_EMPTY = "Select the period of the consent you are applying for";
  private static final String ANNUAL_CONSENT_YEAR_EMPTY = "Year must have a value.";

  private ConsentLengthFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    validator = new ConsentLengthFormValidator();
  }

  @Test
  void validate_completeShortTermAndValidForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getShortTermConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_completeAnnualAndValidForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_completeLongTermAndValidForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_withConsentLengthTypeMissing() {
    ConsentLengthForm form = new ConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("consentLengthType", Collections.singletonList(CONSENT_LENGTH_PERIOD_EMPTY))
    );
  }

  @Test
  void validate_withAnnualConsentYearMissing() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.ANNUAL);

    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_EMPTY))
    );
  }
}