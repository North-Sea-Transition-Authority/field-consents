package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Service
public class TeamMemberViewService {

  private final TeamMemberService teamMemberService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TeamService teamService;

  @Autowired
  public TeamMemberViewService(TeamMemberService teamMemberService,
                               EnergyPortalUserService energyPortalUserService,
                               TeamService teamService) {
    this.teamMemberService = teamMemberService;
    this.energyPortalUserService = energyPortalUserService;
    this.teamService = teamService;
  }

  public List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {
    var members = teamMemberService.getTeamMembers(team);
    return createUserViewsFromTeamMembers(members);
  }

  public Optional<TeamMemberView> getTeamMemberView(TeamMember teamMember) {
    return createUserViewsFromTeamMembers(List.of(teamMember))
        .stream()
        .findFirst();
  }

  private List<TeamMemberView> createUserViewsFromTeamMembers(Collection<TeamMember> teamMembers) {

    // extract list of WUA to lookup
    var webUserAccountIds = teamMembers
        .stream()
        .map(TeamMember::wuaId)
        .toList();

    // Create map of Energy Portal users with WUA as the key for ease of lookup
    var energyPortalUsers = energyPortalUserService.getEnergyPortalUserMap(webUserAccountIds);

    return teamMembers
        .stream()
        .map(teamMember -> createTeamMemberView(teamMember, energyPortalUsers))
        .sorted(Comparator.comparing(TeamMemberView::firstName).thenComparing(TeamMemberView::lastName))
        .toList();
  }

  private TeamMemberView createTeamMemberView(TeamMember teamMember,
                                              Map<WebUserAccountId, EnergyPortalUserDto> energyPortalUsers) {

    if (energyPortalUsers.containsKey(teamMember.wuaId())) {

      var energyPortalUser = energyPortalUsers.get(teamMember.wuaId());

      var roles = teamMember.roles()
          .stream()
          .sorted(Comparator.comparing(TeamRole::getDisplayOrder))
          .collect(Collectors.toCollection(LinkedHashSet::new));

      return new TeamMemberView(
          teamMember.wuaId(),
          teamMember.teamView(),
          energyPortalUser.title(),
          energyPortalUser.forename(),
          energyPortalUser.surname(),
          energyPortalUser.emailAddress(),
          energyPortalUser.telephoneNumber(),
          roles
      );
    } else {
      throw new IllegalArgumentException(
         "Did not find an Energy Portal User with WUA ID %s when converting team members"
             .formatted(teamMember.wuaId())
     );
    }
  }

  public Map<String, String> getUsersMap(List<TeamMemberView> teamMemberViews) {
    return teamMemberViews
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            teamMemberView -> teamMemberView.wuaId().toString(),
            TeamMemberView::getDisplayName));
  }

  private List<TeamMemberView> getTeamMemberViewsWithRolesForTeam(Team team, Set<TeamRole> teamRoles) {
    var members = teamMemberService.getTeamMembers(team)
        .stream()
        .filter(teamMember -> CollectionUtils.containsAny(teamMember.roles(), teamRoles))
        .toList();
    return createUserViewsFromTeamMembers(members);
  }

  public List<TeamMemberView> getTeamMemberViewsWithRolesForTeamType(TeamType teamType, Set<TeamRole> teamRoles) {
    return teamService.getTeamsByType(teamType)
        .stream()
        .map(team -> getTeamMemberViewsWithRolesForTeam(team, teamRoles))
        .flatMap(Collection::stream)
        .toList();
  }
}
