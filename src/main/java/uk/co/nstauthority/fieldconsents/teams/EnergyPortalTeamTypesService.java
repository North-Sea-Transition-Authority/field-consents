package uk.co.nstauthority.fieldconsents.teams;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamTypeService;

@Profile("use-service-access-request")
@Service
public class EnergyPortalTeamTypesService {

  private final EnergyPortalServiceProviderTeamTypeService  serviceProviderTeamTypeRoleService;

  EnergyPortalTeamTypesService(EnergyPortalServiceProviderTeamTypeService serviceProviderTeamTypeRoleService) {
    this.serviceProviderTeamTypeRoleService = serviceProviderTeamTypeRoleService;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  void publishTeamTypesMessage() {
    var teamTypes = Arrays.stream(TeamType.values())
        .map(TeamType::name)
        .collect(Collectors.toSet());
    serviceProviderTeamTypeRoleService.publishTeamTypes(teamTypes);
  }
}
