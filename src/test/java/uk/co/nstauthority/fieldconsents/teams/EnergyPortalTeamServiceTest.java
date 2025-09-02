package uk.co.nstauthority.fieldconsents.teams;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamService;

@ExtendWith(MockitoExtension.class)
class EnergyPortalTeamServiceTest {

  @Mock
  private EnergyPortalServiceProviderTeamService energyPortalServiceProviderTeamService;

  @Mock
  private TeamRepository teamRepository;

  @InjectMocks
  private EnergyPortalTeamService energyPortalTeamService;

  @Test
  void publishSetTeamsMessage() {
    var team1 = TeamTestUtil.newBuilder().build();
    var team2 = TeamTestUtil.newBuilder().build();

    var expectedDto1 = new ServiceProviderTeamDto(
        team1.getId().toString(),
        team1.getScopeId(),
        ScopeType.ORGANISATION_GROUP
    );
    var expectedDto2 = new ServiceProviderTeamDto(
        team2.getId().toString(),
        team2.getScopeId(),
        ScopeType.ORGANISATION_GROUP
    );

    when(teamRepository.findByTeamType(TeamType.INDUSTRY)).thenReturn(List.of(team1, team2));

    energyPortalTeamService.publishAddTeamsMessage();

    verify(energyPortalServiceProviderTeamService).publishTeams(Set.of(expectedDto1, expectedDto2));
  }
}