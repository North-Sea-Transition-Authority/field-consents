package uk.co.nstauthority.fieldconsents.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementException;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@Service
class TeamMemberViewQueryService {

  private final EnergyPortalUserService energyPortalUserService;

  TeamMemberViewQueryService(EnergyPortalUserService energyPortalUserService) {
    this.energyPortalUserService = energyPortalUserService;
  }

  TeamMemberView getTeamMemberView(Team team, Long wuaId) {
    var energyPortalUser = energyPortalUserService.findByWuaId(WebUserAccountId.from(wuaId))
        .orElseThrow(() -> new TeamManagementException("WuaId [%d] not found in Energy Portal response".formatted(wuaId)));

    return TeamMemberView.from(energyPortalUser, team.getId(), Set.of());
  }

  List<TeamMemberView> getTeamMemberViews(Collection<TeamRole> teamRoles) {
    if (teamRoles.isEmpty()) {
      return List.of();
    }

    var teamRolesByWuaId = teamRoles
        .stream()
        .collect(Collectors.groupingBy(TeamRole::getWuaId));

    var webUserAccountIds = teamRolesByWuaId.keySet()
        .stream()
        .map(WebUserAccountId::new)
        .collect(Collectors.toSet());

    var usersByWuaId = energyPortalUserService.findByWuaIds(webUserAccountIds)
        .stream()
        .collect(Collectors.toMap(EnergyPortalUserDto::webUserAccountId, Function.identity()));

    var teamMemberViews = new ArrayList<TeamMemberView>();

    for (var wuaId : teamRolesByWuaId.keySet()) {
      var energyPortalUserDto = usersByWuaId.get(wuaId);
      if (energyPortalUserDto == null) {
        throw new TeamManagementException("WuaId [%d] not found in Energy Portal response".formatted(wuaId));
      }

      var rolesByTeam = teamRolesByWuaId.get(wuaId)
          .stream()
          .collect(Collectors.groupingBy(
              TeamRole::getTeam,
              Collectors.mapping(TeamRole::getRole, Collectors.toSet()))
          );

      for (var entry : rolesByTeam.entrySet()) {
        var team = entry.getKey();
        var roles = entry.getValue()
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        teamMemberViews.add(TeamMemberView.from(energyPortalUserDto, team.getId(), roles));
      }
    }

    return teamMemberViews
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::forename).thenComparing(TeamMemberView::surname))
        .toList();
  }
}
