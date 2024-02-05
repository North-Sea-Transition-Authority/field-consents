package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;

@ExtendWith(MockitoExtension.class)
class TeamManagementServiceTest {

  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;
  private TeamManagementService teamManagementService;

  @BeforeEach
  void setUp() {
    customerBrandingConfigurationProperties = new CustomerBrandingConfigurationProperties(
        "name", "mnem", "business@email.com", "regulator legal name"
    );
    teamManagementService = new TeamManagementService(customerBrandingConfigurationProperties);
  }

  @Test
  void teamsToTeamViews() {
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamViews = teamManagementService.teamsToTeamViews(List.of(secondTeam, firstTeam));

    assertThat(teamViews)
        .containsExactly(
            TeamView.fromTeam(firstTeam, customerBrandingConfigurationProperties),
            TeamView.fromTeam(secondTeam, customerBrandingConfigurationProperties)
        );
  }
}
