package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class VentShortTermFormServiceTest {

  @Mock
  VentShortTermFormValidator ventShortTermFormValidator;

  @InjectMocks
  VentShortTermFormService ventShortTermFormService;

  @Test
  void validate_verifyCallsValidator() {
    var form = new VentShortTermForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var returnedBindingResult = ventShortTermFormService.validate(form, bindingResult);

    verify(ventShortTermFormValidator).validate(form, bindingResult);
    assertThat(returnedBindingResult).isEqualTo(bindingResult);
  }
}
