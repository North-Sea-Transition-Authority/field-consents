package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class FlareAnnualFormServiceTest {

  @Mock
  FlareAnnualFormValidator flareAnnualFormValidator;

  @InjectMocks
  FlareAnnualFormService flareAnnualFormService;

  @Test
  void validate_verifyCallsValidator() {
    var form = new FlareAnnualForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var returnedBindingResult = flareAnnualFormService.validate(form, bindingResult);

    verify(flareAnnualFormValidator).validate(form, bindingResult);
    assertThat(returnedBindingResult).isEqualTo(bindingResult);
  }
}
