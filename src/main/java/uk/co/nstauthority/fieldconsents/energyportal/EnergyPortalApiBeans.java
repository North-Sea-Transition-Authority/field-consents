package uk.co.nstauthority.fieldconsents.energyportal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.energyportalapi.client.EnergyPortal;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;

@Configuration
public class EnergyPortalApiBeans {

  @Bean
  EnergyPortal energyPortal(EnergyPortalApiConfig energyPortalApiConfig) {
    return EnergyPortal.defaultConfiguration(energyPortalApiConfig.url(), energyPortalApiConfig.preSharedKey());
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

}
