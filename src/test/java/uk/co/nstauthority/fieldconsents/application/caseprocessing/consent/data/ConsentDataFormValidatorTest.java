package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class ConsentDataFormValidatorTest {

  @InjectMocks
  private ConsentDataFormValidator validator;

  private ConsentDataForm form;
  private BindingResult bindingResult;

  @BeforeEach
  void setUp() {
    form = new ConsentDataForm(null, null);
    bindingResult = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void supports() {
    assertTrue(validator.supports(ConsentDataForm.class));
  }

  @Test
  void supports_invalidType() {
    assertFalse(validator.supports(String.class));
  }

  @Test
  void validate() {
    form.consentStartDate().setDate(LocalDate.parse("2024-01-01"));
    form.consentEndDate().setDate(LocalDate.parse("2025-01-01"));

    validator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_sameDay() {
    form.consentStartDate().setDate(LocalDate.parse("2024-01-01"));
    form.consentEndDate().setDate(LocalDate.parse("2024-01-01"));

    validator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_noConsentStartDate_noConsentEndDate() {
    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .map(FieldError::getField)
        .allSatisfy(code -> assertTrue(code.startsWith("consentStartDate")));
  }

  @Test
  void validate_noConsentEndDate() {
    form.consentStartDate().setDate(LocalDate.parse("2024-01-01"));

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .map(FieldError::getField)
        .allSatisfy(code -> assertTrue(code.startsWith("consentEndDate")));
  }

  @Test
  void validate_consentEndDateNotAfterConsentStartDate() {
    var date = LocalDate.parse("2024-01-01");

    form.consentStartDate().setDate(date);
    form.consentEndDate().setDate(date);

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .map(FieldError::getField)
        .allSatisfy(code -> assertTrue(code.startsWith("consentEndDate")));
  }

}
