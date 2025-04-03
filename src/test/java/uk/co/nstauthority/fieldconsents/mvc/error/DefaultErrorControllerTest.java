package uk.co.nstauthority.fieldconsents.mvc.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;


@ContextConfiguration(classes = DefaultErrorController.class)
class DefaultErrorControllerTest extends AbstractControllerTest {

  @MockitoBean
  private ErrorService errorService;

  @SecurityTest
  void handleError_notLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DefaultErrorController.class).handleError(null))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/error/notFound"));
  }

  @Test
  void handleError_dispatcherException() throws Exception {
    var exception = new RuntimeException("Something went wrong");

    mockMvc.perform(get(ReverseRouter.route(on(DefaultErrorController.class)
        .handleError(null)))
        .with(user(user))
        .requestAttr(DispatcherServlet.EXCEPTION_ATTRIBUTE, exception)
    );

    verify(errorService).addErrorAttributesToModel(
        any(ModelAndView.class),
        eq(exception),
        any(HttpServletRequest.class)
    );
  }

  @Test
  void handleError_servletException() throws Exception {
    var exception = new RuntimeException("Something went wrong");

    mockMvc.perform(get(ReverseRouter.route(on(DefaultErrorController.class)
        .handleError(null)))
        .with(user(user))
        .requestAttr(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception)
        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.NOT_FOUND.value())
    );

    verify(errorService).addErrorAttributesToModel(
        any(ModelAndView.class),
        eq(exception),
        any(HttpServletRequest.class)
    );
  }

  @ParameterizedTest
  @CsvSource({
      "404, fcs/error/notFound",
      "405, fcs/error/notFound",
      "401, fcs/error/unauthorised",
      "403, fcs/error/unauthorised",
      "500, fcs/error/default",
  })
  void handleError_statusReturnsTemplate(int status, String viewName) throws Exception {
    var exception = new RuntimeException("Something went wrong");

    doAnswer(invocation -> {
      var modelAndView = invocation.getArgument(0, ModelAndView.class);
      modelAndView.addObject("errorRef", "errorReference");
      return null;
    })
        .when(errorService)
        .addErrorAttributesToModel(any(ModelAndView.class), eq(exception), any(HttpServletRequest.class));

    mockMvc.perform(get(ReverseRouter.route(on(DefaultErrorController.class)
        .handleError(null)))
        .with(user(user))
        .requestAttr(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception)
        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, status)
    ).andExpect(view().name(viewName));
  }

}
