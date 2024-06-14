package uk.co.nstauthority.fieldconsents.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("service")
@Validated
public record ServiceConfigurationProperties(
    @NotNull String baseUrl,
    @NotNull Error error,
    @NotNull SupportContact businessSupportContact,
    @NotNull SupportContact technicalSupportContact
) {

  public record SupportContact(
      @NotNull String name,
      @NotNull String email,
      String phone
  ) {
  }

  public record Error(boolean includeStacktrace) {
  }

}
