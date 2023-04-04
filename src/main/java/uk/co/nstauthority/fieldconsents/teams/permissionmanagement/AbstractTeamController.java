package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.exception.FcsEntityNotFoundException;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Component
public abstract class AbstractTeamController {

  private final TeamService teamService;

  protected AbstractTeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  public Team getTeam(TeamId teamId, TeamType teamType) {
    return teamService.getTeam(teamId, teamType)
        .orElseThrow(() -> new FcsEntityNotFoundException(
            "No team with ID [%s] found with TeamType of [%s]".formatted(teamId.id(), teamType.name())
        ));
  }

}
