package uk.co.nstauthority.fieldconsents.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.branding.EnableAllBrandingConfigurationProperties;

@ContextConfiguration(classes = {
    DefaultPageControllerAdvice.class,
    UserDetailService.class
})
@EnableAllBrandingConfigurationProperties
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDefaultPageControllerAdvice {
}
