package uk.co.nstauthority.fieldconsents.mvc.error;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;


@Component
class DefaultExceptionResolver extends SimpleMappingExceptionResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionResolver.class);

  private final ErrorService errorService;

  DefaultExceptionResolver(ErrorService errorService) {
    this.errorService = errorService;
    setDefaultErrorView("fcs/error/default");
    setDefaultStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @Override
  protected ModelAndView getModelAndView(String viewName, Exception exception, HttpServletRequest request) {
    if (exception instanceof ClientAbortException) {
      //See https://mtyurt.net/post/spring-how-to-handle-ioexception-broken-pipe.html
      //ClientAbortException indicates a broken pipe/network error. Return null, so it can be handled by the servlet,
      //otherwise Spring attempts to write to the broken response.
      LOGGER.trace("Suppressed ClientAbortException");
      return null;
    }

    var modelAndView = super.getModelAndView(viewName, exception);
    errorService.addErrorAttributesToModel(modelAndView, exception, request);

    return modelAndView;
  }

}
