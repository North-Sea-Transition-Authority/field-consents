package uk.co.nstauthority.fieldconsents.teams;

import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamService;

@Profile("use-service-access-request")
@Service
public class EnergyPortalTeamService {

  private final EnergyPortalServiceProviderTeamService energyPortalServiceProviderTeamService;
  private final TeamRepository teamRepository;

  EnergyPortalTeamService(
      EnergyPortalServiceProviderTeamService energyPortalServiceProviderTeamService,
      TeamRepository teamRepository
  ) {
    this.energyPortalServiceProviderTeamService = energyPortalServiceProviderTeamService;
    this.teamRepository = teamRepository;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void publishAddTeamsMessage() {
    var industryTeamDtos = teamRepository.findByTeamType(TeamType.INDUSTRY)
        .stream()
        .map(team -> new ServiceProviderTeamDto(
            team.getId().toString(),
            team.getScopeId(),
            ScopeType.ORGANISATION_GROUP,
            team.getTeamType().name()
        ))
        .collect(Collectors.toSet());

    energyPortalServiceProviderTeamService.publishTeams(industryTeamDtos);
  }
}
