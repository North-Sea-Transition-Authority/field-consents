package uk.co.nstauthority.fieldconsents.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.topnavigation.TopNavigationService;

@ContextConfiguration(classes = {
    DefaultPageControllerAdvice.class,
    TopNavigationService.class,
    UserDetailService.class
})
@IncludeServiceBrandingConfigurationProperties
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDefaultPageControllerAdvice {
}
