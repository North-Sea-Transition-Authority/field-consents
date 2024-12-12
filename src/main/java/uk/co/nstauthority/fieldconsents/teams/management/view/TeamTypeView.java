package uk.co.nstauthority.fieldconsents.teams.management.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.jetbrains.annotations.NotNull;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementController;

public record TeamTypeView(
    String teamTypeName,
    String manageUrl
) implements Comparable<TeamTypeView> {

  public static TeamTypeView from(TeamType teamType) {
    return new TeamTypeView(
        teamType.getDisplayName(),
        ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(teamType.getUrlSlug(), null))
    );
  }

  @Override
  public int compareTo(@NotNull TeamTypeView other) {
    return teamTypeName.compareTo(other.teamTypeName);
  }
}
