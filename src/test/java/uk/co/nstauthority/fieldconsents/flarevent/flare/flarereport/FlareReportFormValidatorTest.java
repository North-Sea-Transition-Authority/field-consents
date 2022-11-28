package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

class FlareReportFormValidatorTest {

  private FlareReportFormValidator validator;

  private Errors errors;

  @BeforeEach
  void setUp() {
    var flareVentRowFormValidator = new FlareVentRowFormValidator();
    var flareReportMonthFormValidator = new FlareReportMonthFormValidator(flareVentRowFormValidator);
    validator = new FlareReportFormValidator(flareReportMonthFormValidator);
  }

  @Test
  void validate_stubForm() {
    FlareReportForm flareReportForm = FlareReportTestUtil.getStubFlareReportForm();
    errors = new BeanPropertyBindingResult(flareReportForm, "form");

    validator.validate(flareReportForm, errors);

    // just check that we have errors for all months for all fields
    // (i.e. 5 fields across 12 months in the stub form)
    // the individual validators have been tested separately
    assertThat(errors.getErrorCount()).isEqualTo(60);
  }

  @Test
  void validate_validForm() {
    FlareReportForm flareReportForm = FlareReportTestUtil.getFullFlareReportFormForYear(2022);
    errors = new BeanPropertyBindingResult(flareReportForm, "form");

    validator.validate(flareReportForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void supports() {
    assertThat(validator.supports(FlareReportForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(validator.supports(Integer.class)).isFalse();
  }


}
