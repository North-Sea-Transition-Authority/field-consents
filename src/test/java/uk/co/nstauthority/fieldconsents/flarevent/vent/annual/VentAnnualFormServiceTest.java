package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class VentAnnualFormServiceTest {

  @Mock
  VentAnnualFormValidator ventAnnualFormValidator;

  @InjectMocks
  VentAnnualFormService ventAnnualFormService;

  @Test
  void validate_verifyCallsValidator() {
    var form = new VentAnnualForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var returnedBindingResult = ventAnnualFormService.validate(form, bindingResult);

    verify(ventAnnualFormValidator).validate(form, bindingResult);
    assertThat(returnedBindingResult).isEqualTo(bindingResult);
  }
}
