package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

class VentReportFormValidatorTest {

  private VentReportFormValidator validator;

  private Errors errors;

  @BeforeEach
  void setUp() {
    var ventVentRowFormValidator = new FlareVentRowFormValidator();
    var ventReportMonthFormValidator = new VentReportMonthFormValidator(ventVentRowFormValidator);
    validator = new VentReportFormValidator(ventReportMonthFormValidator);
  }

  @Test
  void validate_stubForm() {
    VentReportForm ventReportForm = VentReportTestUtil.getStubVentReportForm();
    errors = new BeanPropertyBindingResult(ventReportForm, "form");

    validator.validate(ventReportForm, errors);

    // just check that we have errors for all months for all fields
    // (i.e. 5 fields across 12 months in the stub form)
    // the individual validators have been tested separately
    assertThat(errors.getErrorCount()).isEqualTo(60);
  }

  @Test
  void validate_validForm() {
    VentReportForm ventReportForm = VentReportTestUtil.getFullVentReportFormForYear(2022);
    errors = new BeanPropertyBindingResult(ventReportForm, "form");

    validator.validate(ventReportForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void supports() {
    assertThat(validator.supports(VentReportForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(validator.supports(Integer.class)).isFalse();
  }


}
