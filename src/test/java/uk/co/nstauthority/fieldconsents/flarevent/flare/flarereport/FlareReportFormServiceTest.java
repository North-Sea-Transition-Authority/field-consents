package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class FlareReportFormServiceTest {

  @Mock
  FlareReportFormValidator flareReportFormValidator;

  @InjectMocks
  FlareReportFormService flareReportFormService;

  @Test
  void validate_verifyCallsValidator() {
    var form = new FlareReportForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var returnedBindingResult = flareReportFormService.validate(form, bindingResult);

    verify(flareReportFormValidator).validate(form, bindingResult);
    assertThat(returnedBindingResult).isEqualTo(bindingResult);
  }
}
