package uk.co.nstauthority.fieldconsents.teams;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamTypeRoleDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamTypeRoleService;

@Profile("use-service-access-request")
@Service
public class EnergyPortalTeamTypeRoleService {

  private final EnergyPortalServiceProviderTeamTypeRoleService serviceProviderTeamTypeRoleService;

  EnergyPortalTeamTypeRoleService(EnergyPortalServiceProviderTeamTypeRoleService serviceProviderTeamTypeRoleService) {
    this.serviceProviderTeamTypeRoleService = serviceProviderTeamTypeRoleService;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  void publishRolesForTeamTypeMessage() {
    for (var teamType : TeamType.values()) {
      var serviceRoleDtos = teamType.getAllowedRoles()
          .stream()
          .map(role -> new ServiceProviderTeamTypeRoleDto(
                  role.name(),
                  role.getDisplayName(),
                  role.getDescription(),
                  role == Role.ACCESS_MANAGER,
                  Arrays.stream(Role.values()).toList().indexOf(role)
              )
          ).collect(Collectors.toSet());

      serviceProviderTeamTypeRoleService.publishRolesForTeamType(serviceRoleDtos, teamType.name());
    }
  }
}
