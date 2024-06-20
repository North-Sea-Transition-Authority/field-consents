package uk.co.nstauthority.fieldconsents.energyportal;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "energy-portal")
@Validated
public record EnergyPortalConfiguration(
    @NotNull String registrationUrl,
    @NotNull String logoutUrl,
    @NotNull String logoutPreSharedKey
) {
}
