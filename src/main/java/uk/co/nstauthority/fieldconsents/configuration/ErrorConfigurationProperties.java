package uk.co.nstauthority.fieldconsents.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service.error")
public record ErrorConfigurationProperties(boolean includeStacktrace) {
}
