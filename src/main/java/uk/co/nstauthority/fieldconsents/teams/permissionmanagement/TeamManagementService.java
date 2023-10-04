package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamView;

@Service
public class TeamManagementService {
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @Autowired
  TeamManagementService(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  public List<TeamView> teamsToTeamViews(List<Team> teams) {
    return teams.stream()
        .map(team -> TeamView.fromTeam(team, customerBrandingConfigurationProperties))
        .sorted(TeamView.sort())
        .toList();
  }
}
