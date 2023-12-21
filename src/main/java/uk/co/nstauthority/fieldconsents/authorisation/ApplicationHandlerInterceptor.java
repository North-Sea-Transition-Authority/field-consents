package uk.co.nstauthority.fieldconsents.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.rules.ApplicationInterceptorSecurityRule;
import uk.co.nstauthority.fieldconsents.mvc.AbstractHandlerInterceptor;

@Component
public class ApplicationHandlerInterceptor extends AbstractHandlerInterceptor {

  private final ApplicationVersionService applicationVersionService;
  private final UserDetailService userDetailService;
  private final List<ApplicationInterceptorSecurityRule> securityRules;

  @Autowired
  public ApplicationHandlerInterceptor(ApplicationVersionService applicationVersionService,
                                       UserDetailService userDetailService,
                                       List<ApplicationInterceptorSecurityRule> securityRules) {
    this.applicationVersionService = applicationVersionService;
    this.userDetailService = userDetailService;
    this.securityRules = securityRules;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler
  ) throws IOException {
    if (handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    var handlerMethod = (HandlerMethod) handler;
    var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    var annotation = findMethodOrClassAnnotation(Security.class, handlerMethod);

    if (annotation == null) {
      throw new IllegalStateException("Controllers must be annotated with @Security");
    }

    if (annotation.disable()) {
      return true;
    }

    var user = userDetailService.getUserDetail();
    var applicationId = pathVariables.get("applicationId");

    if (applicationId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Received request with no applicationId present");
    }

    var applicationVersionOptional = applicationVersionService.findLatestApplicationVersion(
        Integer.valueOf(applicationId));
    if (applicationVersionOptional.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "Received request with non-existent application id %s".formatted(applicationId));
    }

    for (var securityRule : securityRules) {
      var annotationObject = findMethodOrClassAnnotation(securityRule.supports(), handlerMethod);

      if (annotationObject == null) {
        continue;
      }

      var result = securityRule.check(
          annotationObject,
          request,
          response,
          user,
          applicationVersionOptional.get()
      );

      var hasRulePassed = processRedirectsAndReturnResult(result, response);
      if (!hasRulePassed) {
        return false;
      }
    }
    return true;
  }
}
