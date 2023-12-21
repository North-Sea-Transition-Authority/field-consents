package uk.co.nstauthority.fieldconsents.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.configuration.ErrorConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ControllerAdviceService;

@ExtendWith(MockitoExtension.class)
class ErrorServiceTest {

  @Mock
  private ErrorConfigurationProperties errorConfigurationProperties;

  @Mock
  private ControllerAdviceService controllerAdviceService;

  @InjectMocks
  private ErrorService errorService;

  @Mock
  private HttpServletRequest request;

  private final Throwable throwable = new RuntimeException("Something went wrong...");

  @Test
  void addErrorAttributesToModel_includeStackTrace() {
    when(errorConfigurationProperties.includeStacktrace()).thenReturn(true);

    var modelAndView = new ModelAndView();
    assertThat(errorService.addErrorAttributesToModel(modelAndView, throwable, request)).isEqualTo(modelAndView);

    assertThat(modelAndView.getModel())
        .containsKey("errorRef")
        .containsEntry("stackTrace", ExceptionUtils.getStackTrace(throwable));

    verify(controllerAdviceService).addDefaultModelAttributes(modelAndView, request);
  }

  @Test
  void addErrorAttributesToModel_dontIncludeStackTrace() {
    when(errorConfigurationProperties.includeStacktrace()).thenReturn(false);

    var modelAndView = new ModelAndView();
    assertThat(errorService.addErrorAttributesToModel(modelAndView, throwable, request)).isEqualTo(modelAndView);

    assertThat(modelAndView.getModel()).containsOnlyKeys("errorRef");

    verify(controllerAdviceService).addDefaultModelAttributes(modelAndView, request);
  }

  @Test
  void addErrorAttributesToModel_noThrowable() {
    var modelAndView = new ModelAndView();

    assertThat(errorService.addErrorAttributesToModel(modelAndView, null, request)).isEqualTo(modelAndView);

    verify(controllerAdviceService).addDefaultModelAttributes(modelAndView, request);
  }

  @Test
  void generateErrorReference() {
    assertThat(errorService.generateErrorReference()).hasSize(9);
  }
}
