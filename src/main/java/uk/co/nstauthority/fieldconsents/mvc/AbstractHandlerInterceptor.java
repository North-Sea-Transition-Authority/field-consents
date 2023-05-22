package uk.co.nstauthority.fieldconsents.mvc;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;

public abstract class AbstractHandlerInterceptor implements HandlerInterceptor {

  public boolean hasAnnotations(HandlerMethod handlerMethod,
                                Set<Class<? extends Annotation>> annotationClasses) {
    return annotationClasses
        .stream()
        .anyMatch(annotationClass -> hasAnnotation(handlerMethod, annotationClass));
  }

  public boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return handlerMethod.hasMethodAnnotation(annotation)
        || handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(annotation);
  }

  public Annotation getAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return Objects.requireNonNullElse(
        handlerMethod.getMethodAnnotation(annotation),
        handlerMethod.getMethod().getDeclaringClass().getAnnotation(annotation)
    );
  }

  public static Optional<Parameter> getPathVariableByClass(HandlerMethod handlerMethod, Class<?> clazzOfPathVariable) {
    return Arrays.stream(handlerMethod.getMethod().getParameters())
        .filter(methodParameter -> methodParameter.getType().equals(clazzOfPathVariable)
            && methodParameter.isAnnotationPresent(PathVariable.class))
        .findFirst();
  }

  public static <A extends Annotation> A findMethodOrClassAnnotation(Class<A> annotationClass,
                                                                     HandlerMethod handlerMethod) {
    var method = handlerMethod.getMethod();
    // Check if the method has the desired annotation
    var methodAnnotation = AnnotationUtils.findAnnotation(method, annotationClass);
    if (methodAnnotation != null) {
      return methodAnnotation;
    }

    // Fallback and check if the class contains the annotation
    return AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationClass);
  }

  public static boolean processRedirectsAndReturnResult(SecurityRuleResult securityRuleResult,
                                                        HttpServletResponse response) throws IOException {
    if (!securityRuleResult.hasRulePassed()) {
      var redirectUrl = securityRuleResult.redirectUrl();
      if (redirectUrl != null) {
        response.sendRedirect(redirectUrl);
      }
      var failureStatus = securityRuleResult.failureStatus();
      if (failureStatus != null) {
        throw new ResponseStatusException(failureStatus, securityRuleResult.failureMessage());
      }
      return false;
    }
    return true;
  }
}
