package uk.co.nstauthority.fieldconsents.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("analytics")
@Validated
public record AnalyticsConfigurationProperties(
    @NotBlank String serviceAnalyticIdentifier,
    @NotBlank String energyPortalAnalyticIdentifier
) {
}
