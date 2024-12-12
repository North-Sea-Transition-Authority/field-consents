package uk.co.nstauthority.fieldconsents.teams.management.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.jetbrains.annotations.NotNull;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementController;

public record TeamView(
    String teamName,
    String manageUrl
) implements Comparable<TeamView> {

  public static TeamView from(Team team) {
    return new TeamView(
        team.getName(),
        ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null))
    );
  }

  @Override
  public int compareTo(@NotNull TeamView other) {
    return teamName.compareTo(other.teamName);
  }
}
