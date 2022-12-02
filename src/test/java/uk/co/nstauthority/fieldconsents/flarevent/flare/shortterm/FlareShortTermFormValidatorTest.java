package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

class FlareShortTermFormValidatorTest {

  private FlareShortTermFormValidator validator;

  private Errors errors;

  @BeforeEach
  void setUp() {
    var flareVentRowFormValidator = new FlareVentRowFormValidator();
    validator = new FlareShortTermFormValidator(flareVentRowFormValidator);
  }

  @Test
  void validate_stubForm() {
    FlareShortTermForm flareShortTermForm =
        FlareShortTermTestUtil.getStubFlareShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 5),
            LocalDate.of(2023, Month.JANUARY, 10));
    errors = new BeanPropertyBindingResult(flareShortTermForm, "form");

    validator.validate(flareShortTermForm, errors);

    // just check that we have errors for all months for all fields
    // (i.e. 4 fields across 10 months in the stub form)
    // the individual validators have been tested separately
    assertThat(errors.getErrorCount()).isEqualTo(40);
  }

  @Test
  void validate_validForm() {
    FlareShortTermForm flareShortTermForm =
        FlareShortTermTestUtil.getFullFlareShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 5),
            LocalDate.of(2023, Month.JANUARY, 10));
    errors = new BeanPropertyBindingResult(flareShortTermForm, "form");

    validator.validate(flareShortTermForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void supports() {
    assertThat(validator.supports(FlareShortTermForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(validator.supports(String.class)).isFalse();
  }

}
