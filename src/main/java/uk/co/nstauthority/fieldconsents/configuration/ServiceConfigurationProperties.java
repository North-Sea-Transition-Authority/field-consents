package uk.co.nstauthority.fieldconsents.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "service")
@Validated
public record ServiceConfigurationProperties(String baseUrl) {
}
