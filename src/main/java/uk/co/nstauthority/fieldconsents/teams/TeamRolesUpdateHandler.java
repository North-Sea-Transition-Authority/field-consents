package uk.co.nstauthority.fieldconsents.teams;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamRolesEpasMessage;
import uk.co.fivium.energyportal.starter.configuration.EnergyPortalAccountsConfigurationProperties;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamRolesUpdateHandler;
import uk.co.nstauthority.fieldconsents.audit.AuditRevisionUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementService;

@Profile("use-service-access-request")
@Component
public class TeamRolesUpdateHandler implements EnergyPortalServiceProviderTeamRolesUpdateHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TeamRolesUpdateHandler.class);

  private final EnergyPortalUserService energyPortalUserService;
  private final TeamManagementService teamManagementService;
  private final String serviceName;

  TeamRolesUpdateHandler(EnergyPortalUserService energyPortalUserService,
                         TeamManagementService teamManagementService,
                         EnergyPortalAccountsConfigurationProperties energyPortalAccountsConfigurationProperties
  ) {
    this.energyPortalUserService = energyPortalUserService;
    this.teamManagementService = teamManagementService;
    this.serviceName = energyPortalAccountsConfigurationProperties.serviceName();
  }

  @Override
  public void accept(ServiceProviderTeamRolesEpasMessage serviceProviderTeamRolesEpasMessage) {
    if (!serviceName.equals(serviceProviderTeamRolesEpasMessage.getService())) {
      return;
    }

    var serviceProviderUserTeamRolesDto = serviceProviderTeamRolesEpasMessage.getServiceProviderUserTeamRolesDto();
    var optionalTeam = teamManagementService.getTeam(UUID.fromString(serviceProviderUserTeamRolesDto.teamId()));

    if (optionalTeam.isEmpty()) {
      LOGGER.error("Team not found for id: {}, when updating team_roles from epas team roles update message. correlationId: {}",
          serviceProviderUserTeamRolesDto.teamId(),
          serviceProviderTeamRolesEpasMessage.getCorrelationId()
      );
      return;
    }

    var invokingUser = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(serviceProviderTeamRolesEpasMessage.getDeciderWuaId()));

    AuditRevisionUtil.withFallbackAuditUser(
        ServiceUserDetail.from(invokingUser),
        () -> teamManagementService.setUserTeamRoles(
            serviceProviderUserTeamRolesDto.wuaId(),
            optionalTeam.get(),
            serviceProviderUserTeamRolesDto.roles().stream().map(Role::valueOf).toList())
    );
  }
}
