package uk.co.nstauthority.fieldconsents.branding;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "branding.customer")
@Validated
public record CustomerBrandingConfigurationProperties(
    @NotNull String name,
    @NotNull String mnemonic,
    @NotNull String email,
    @NotNull String teamName,
    @NotNull String legalName,
    @NotNull String legalMnemonic,
    @NotNull String registeredNumber,
    @NotNull String vatNumber,
    @NotNull String address
) {}
