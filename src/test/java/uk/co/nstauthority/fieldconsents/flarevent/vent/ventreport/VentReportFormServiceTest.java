package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class VentReportFormServiceTest {

  @Mock
  VentReportFormValidator ventReportFormValidator;

  @InjectMocks
  VentReportFormService ventReportFormService;

  @Test
  void validate_verifyCallsValidator() {
    var form = new VentReportForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var returnedBindingResult = ventReportFormService.validate(form, bindingResult);

    verify(ventReportFormValidator).validate(form, bindingResult);
    assertThat(returnedBindingResult).isEqualTo(bindingResult);
  }
}
