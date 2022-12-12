package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

class VentAnnualFormValidatorTest {

  private VentAnnualFormValidator validator;

  private Errors errors;

  @BeforeEach
  void setUp() {
    var ventVentRowFormValidator = new FlareVentRowFormValidator();
    validator = new VentAnnualFormValidator(ventVentRowFormValidator);
  }

  @Test
  void validate_stubForm() {
    VentAnnualForm ventAnnualForm = VentAnnualTestUtil.getStubVentAnnualFormForYear(2022);
    errors = new BeanPropertyBindingResult(ventAnnualForm, "form");

    validator.validate(ventAnnualForm, errors);

    // just check that we have errors for all months for all fields
    // (i.e. 4 fields across 12 months in the stub form)
    // the individual validators have been tested separately
    assertThat(errors.getErrorCount()).isEqualTo(48);
  }

  @Test
  void validate_validForm() {
    VentAnnualForm ventAnnualForm = VentAnnualTestUtil.getFullVentAnnualFormForYear(2022);
    errors = new BeanPropertyBindingResult(ventAnnualForm, "form");

    validator.validate(ventAnnualForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void supports() {
    assertThat(validator.supports(VentAnnualForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(validator.supports(String.class)).isFalse();
  }

}
