package uk.co.nstauthority.fieldconsents.mvc.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

@Controller
public class DefaultErrorController implements ErrorController {

  private final ErrorService errorService;

  DefaultErrorController(ErrorService errorService) {
    this.errorService = errorService;
  }

  /**
   * Handles framework-level errors (404s, authorisation failures, filter exceptions) for browser clients. Errors thrown
   * by app code (controller methods and below) are handled in DefaultExceptionResolver.
   */
  @GetMapping("error")
  public ModelAndView handleError(HttpServletRequest request) {
    var modelAndView = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
        .map(Integer.class::cast)
        .map(this::getModelAndViewForStatus)
        .orElse(new ModelAndView("fcs/error/notFound")); // when this endpoint is hit intentionally, show the not found page

    var dispatcherException = request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
    var servletException = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
    var throwable = (Throwable) ObjectUtils.defaultIfNull(dispatcherException, servletException);

    errorService.addErrorAttributesToModel(modelAndView, throwable, request);

    return modelAndView;
  }

  private ModelAndView getModelAndViewForStatus(int statusCode) {
    return switch (HttpStatus.valueOf(statusCode)) {
      case NOT_FOUND, METHOD_NOT_ALLOWED -> new ModelAndView("fcs/error/notFound");
      case FORBIDDEN, UNAUTHORIZED -> new ModelAndView("fcs/error/unauthorised");
      default -> new ModelAndView("fcs/error/default");
    };
  }

}
