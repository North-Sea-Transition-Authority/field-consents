package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

class FlareAnnualFormValidatorTest {

  private FlareAnnualFormValidator validator;

  private Errors errors;

  @BeforeEach
  void setUp() {
    var flareVentRowFormValidator = new FlareVentRowFormValidator();
    var flareAnnualMonthFormValidator = new FlareAnnualMonthFormValidator(flareVentRowFormValidator);
    validator = new FlareAnnualFormValidator(flareAnnualMonthFormValidator);
  }

  @Test
  void validate_stubForm() {
    FlareAnnualForm flareAnnualForm = FlareAnnualTestUtil.getStubFlareAnnualFormForYear(2022);
    errors = new BeanPropertyBindingResult(flareAnnualForm, "form");

    validator.validate(flareAnnualForm, errors);

    // just check that we have errors for all months for all fields
    // (i.e. 4 fields across 12 months in the stub form)
    // the individual validators have been tested separately
    assertThat(errors.getErrorCount()).isEqualTo(48);
  }

  @Test
  void validate_validForm() {
    FlareAnnualForm flareAnnualForm = FlareAnnualTestUtil.getFullFlareAnnualFormForYear(2022);
    errors = new BeanPropertyBindingResult(flareAnnualForm, "form");

    validator.validate(flareAnnualForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void supports() {
    assertThat(validator.supports(FlareAnnualForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(validator.supports(String.class)).isFalse();
  }

}
