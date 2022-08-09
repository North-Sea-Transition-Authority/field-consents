package uk.co.nstauthority.fieldconsents.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {
    DefaultPageControllerAdvice.class
// TODO will likely be added later    TopNavigationService.class
})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDefaultPageControllerAdvice {
}
