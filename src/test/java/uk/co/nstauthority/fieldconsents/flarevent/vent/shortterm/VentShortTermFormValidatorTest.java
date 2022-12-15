package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

class VentShortTermFormValidatorTest {

  private VentShortTermFormValidator validator;

  private Errors errors;

  @BeforeEach
  void setUp() {
    var flareVentRowFormValidator = new FlareVentRowFormValidator();
    validator = new VentShortTermFormValidator(flareVentRowFormValidator);
  }

  @Test
  void validate_stubForm() {
    VentShortTermForm ventShortTermForm =
        VentShortTermTestUtil.getStubVentShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 5),
            LocalDate.of(2023, Month.JANUARY, 10));
    errors = new BeanPropertyBindingResult(ventShortTermForm, "form");

    validator.validate(ventShortTermForm, errors);

    // just check that we have errors for all months for all fields
    // (i.e. 4 fields across 10 months in the stub form)
    // the individual validators have been tested separately
    assertThat(errors.getErrorCount()).isEqualTo(40);
  }

  @Test
  void validate_validForm() {
    VentShortTermForm ventShortTermForm =
        VentShortTermTestUtil.getFullVentShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 5),
            LocalDate.of(2023, Month.JANUARY, 10));
    errors = new BeanPropertyBindingResult(ventShortTermForm, "form");

    validator.validate(ventShortTermForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void supports() {
    assertThat(validator.supports(VentShortTermForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(validator.supports(String.class)).isFalse();
  }

}
