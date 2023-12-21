package uk.co.nstauthority.fieldconsents.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultExceptionResolverTest {

  @Mock
  private ErrorService errorService;

  @InjectMocks
  private DefaultExceptionResolver defaultExceptionResolver;

  @Mock
  private HttpServletRequest request;

  @Test
  void getModelAndView_clientAbortException() {
    var viewName = "fcs/example/form";
    var exception = new ClientAbortException("Something went wrong...");

    assertThat(defaultExceptionResolver.getModelAndView(viewName, exception, request)).isNull();
  }

  @Test
  void getModelAndView_runtimeException() {
    var viewName = "fcs/example/form";
    var exception = new RuntimeException("Something went wrong...");

    var modelAndView = defaultExceptionResolver.getModelAndView(viewName, exception, request);

    verify(errorService).addErrorAttributesToModel(modelAndView, exception, request);
  }
}
