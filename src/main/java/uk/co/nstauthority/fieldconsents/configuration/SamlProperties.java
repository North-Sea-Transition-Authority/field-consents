package uk.co.nstauthority.fieldconsents.configuration;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "saml")
@Validated
record SamlProperties(@NotNull String registrationId,
                      @NotNull String entityId,
                      @NotNull String certificate,
                      @NotNull String loginUrl,
                      @NotNull String consumerServiceLocation) {
}
