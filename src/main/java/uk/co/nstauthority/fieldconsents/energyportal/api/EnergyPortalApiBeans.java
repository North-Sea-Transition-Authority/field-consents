package uk.co.nstauthority.fieldconsents.energyportal.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.energyportalapi.client.EnergyPortal;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.client.pets.PetsApplicationApi;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;

@Configuration
public class EnergyPortalApiBeans {

  @Bean
  EnergyPortal energyPortal(EnergyPortalApiConfig energyPortalApiConfig) {
    return EnergyPortal.customConfiguration(
        energyPortalApiConfig.url(),
        energyPortalApiConfig.preSharedKey(),
        EnergyPortal.DEFAULT_REQUEST_TIMEOUT_SECONDS,
        () -> new LogCorrelationId(CorrelationIdUtil.getCorrelationIdFromMdc())
        );
  }

  @Bean
  public OrganisationApi organisationApi(EnergyPortal energyPortal) {
    return new OrganisationApi(energyPortal);
  }

  @Bean
  public FieldApi fieldApi(EnergyPortal energyPortal) {
    return new FieldApi(energyPortal);
  }

  @Bean
  public TerminalApi terminalApi(EnergyPortal energyPortal) {
    return new TerminalApi(energyPortal);
  }

  @Bean
  public PetsApplicationApi petsApplicationApi(EnergyPortal energyPortal) {
    return new PetsApplicationApi(energyPortal);
  }

  @Bean
  UserApi userApi(EnergyPortal energyPortal) {
    return new UserApi(energyPortal);
  }
}
