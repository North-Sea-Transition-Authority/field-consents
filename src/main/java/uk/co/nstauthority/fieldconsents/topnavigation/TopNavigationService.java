package uk.co.nstauthority.fieldconsents.topnavigation;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class TopNavigationService {

  private final TeamQueryService teamQueryService;

  TopNavigationService(TeamQueryService teamQueryService) {
    this.teamQueryService = teamQueryService;
  }

  public List<TopNavigationItem> getTopNavigationItems(ServiceUserDetail user) {
    if (user == null) {
      return EnumSet.allOf(TopNavigationItem.class)
          .stream()
          .filter(item -> item.getRolesByTeamType() == null)
          .toList();
    }

    var rolesByTeamType = teamQueryService.getTeamRoles(user)
        .stream()
        .collect(Collectors.groupingBy(
            teamRole -> teamRole.getTeam().getTeamType(),
            Collectors.mapping(TeamRole::getRole, Collectors.toSet())
        ));

    return EnumSet.allOf(TopNavigationItem.class)
        .stream()
        .filter(item -> {
          if (item.getRolesByTeamType() == null) {
            return true;
          }

          for (var teamType: TeamType.values()) {
            if (CollectionUtils.containsAny(rolesByTeamType.get(teamType), item.getRoles(teamType))) {
              return true;
            }
          }

          return false;
        })
        .toList();
  }
}
