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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
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
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler
  ) throws IOException {
    if (handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    var handlerMethod = (HandlerMethod) handler;
    var annotation = findMethodOrClassAnnotation(Security.class, handlerMethod);

    if (annotation == null) {
      throw new IllegalStateException("Controllers must be annotated with @Security");
    }

    if (annotation.disable()) {
      return true;
    }

    var applicationId = findPathVariableInt(request, "applicationId");
    var applicationVersionId = findPathVariableInt(request, "applicationVersionId");

    ApplicationVersion applicationVersion;

    if (applicationId != null) {
      applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    } else if (applicationVersionId != null) {
      applicationVersion = applicationVersionService.getApplicationVersionById(applicationVersionId);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find application version");
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
          userDetailService.getUserDetail(),
          applicationVersion
      );

      var hasRulePassed = processRedirectsAndReturnResult(result, response);
      if (!hasRulePassed) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private Integer findPathVariableInt(HttpServletRequest request, String pathVariable) {
    var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    var value = pathVariables.get(pathVariable);

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

}
