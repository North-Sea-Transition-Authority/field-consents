package uk.co.nstauthority.fieldconsents.branding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = {
    CustomerBrandingConfigurationProperties.class,
    ServiceBrandingConfigurationProperties.class,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAllBrandingConfigurationProperties {
}
