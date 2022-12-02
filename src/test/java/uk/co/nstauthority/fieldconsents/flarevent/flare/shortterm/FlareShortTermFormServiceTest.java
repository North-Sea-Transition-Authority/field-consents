package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class FlareShortTermFormServiceTest {

  @Mock
  FlareShortTermFormValidator flareShortTermFormValidator;

  @InjectMocks
  FlareShortTermFormService flareShortTermFormService;

  @Test
  void validate_verifyCallsValidator() {
    var form = new FlareShortTermForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var returnedBindingResult = flareShortTermFormService.validate(form, bindingResult);

    verify(flareShortTermFormValidator).validate(form, bindingResult);
    assertThat(returnedBindingResult).isEqualTo(bindingResult);
  }
}
