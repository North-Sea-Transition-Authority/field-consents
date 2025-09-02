package uk.co.nstauthority.fieldconsents.teams;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderUserRolesService;

@Profile("use-service-access-request")
@Service
public class EnergyPortalUserRoleService {

  private final TeamRoleRepository teamRoleRepository;
  private final EnergyPortalServiceProviderUserRolesService energyPortalServiceProviderUserRolesService;
  
  EnergyPortalUserRoleService(
      TeamRoleRepository teamRoleRepository,
      EnergyPortalServiceProviderUserRolesService energyPortalServiceProviderUserRolesService
  ) {
    this.teamRoleRepository = teamRoleRepository;
    this.energyPortalServiceProviderUserRolesService = energyPortalServiceProviderUserRolesService;
  }

  @EventListener(ApplicationReadyEvent.class)
  void publishAllUserTeamRolesMessage() {
    var wuaIdToTeamRoles = teamRoleRepository.findAll()
        .stream()
        .collect(groupingBy(TeamRole::getWuaId, toSet()));

    for (var wuaIdToTeamRoleEntry : wuaIdToTeamRoles.entrySet()) {
      var teamToTeamRoles = wuaIdToTeamRoleEntry.getValue()
          .stream()
          .collect(groupingBy(TeamRole::getTeam, toSet()));

      for (var teamToTeamRoleEntry : teamToTeamRoles.entrySet()) {
        var wuaId = wuaIdToTeamRoleEntry.getKey();
        var team = teamToTeamRoleEntry.getKey();
        var roles = teamToTeamRoleEntry.getValue()
            .stream()
            .map(teamRole -> teamRole.getRole().name())
            .collect(Collectors.toSet());

        energyPortalServiceProviderUserRolesService.publishUsersRolesForTeam(
            wuaId,
            team.getId().toString(),
            team.getTeamType().name(),
            roles
        );
      }
    }
  }
}
