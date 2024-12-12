package uk.co.nstauthority.fieldconsents.authorisation;

import java.lang.annotation.Annotation;
import java.util.Optional;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;

public final class HandlerInterceptorUtil {

  public static <T extends Annotation> Optional<T> findAnnotation(HandlerMethod handlerMethod, Class<T> annotation) {
    var methodAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation);
    if (methodAnnotation != null) {
      return Optional.of(methodAnnotation);
    }

    var classAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation);
    if (classAnnotation != null) {
      return Optional.of(classAnnotation);
    }

    return Optional.empty();
  }

  private HandlerInterceptorUtil() {
    throw new UnsupportedOperationException();
  }

}
