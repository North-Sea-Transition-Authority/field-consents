package uk.co.nstauthority.fieldconsents.application;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application")
@Validated
public record ApplicationConfigurationProperties(
    @NotNull
    String applicationNoStartValue
) {}

