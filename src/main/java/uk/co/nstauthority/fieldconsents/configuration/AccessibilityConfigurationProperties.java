package uk.co.nstauthority.fieldconsents.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("accessibility")
@Validated
public record AccessibilityConfigurationProperties(
    @NotBlank String statementPreparedDate,
    @NotBlank String statementLastReviewDate,
    @NotBlank String serviceLastTestDate,
    @NotBlank String serviceLastTestedBy,
    @NotBlank String designSystemLastTestDate,
    @NotBlank String productionGuidanceUrl,
    @NotBlank String flareAndVentGuidanceUrl
) {
}
