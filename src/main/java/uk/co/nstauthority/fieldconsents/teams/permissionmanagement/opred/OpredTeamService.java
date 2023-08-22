package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class OpredTeamService {

  private final TeamService teamService;
  private final TeamMemberService teamMemberService;
  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  OpredTeamService(TeamService teamService,
                   TeamMemberService teamMemberService,
                   TeamMemberRoleService teamMemberRoleService) {
    this.teamService = teamService;
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  public boolean isAccessManager(TeamId teamId, ServiceUserDetail user) {
    return teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(OpredTeamRole.ACCESS_MANAGER.name()));
  }

  Optional<Team> getTeam(TeamId teamId) {
    return teamService.getTeam(teamId, TeamType.OPRED);
  }

  List<Team> getTeamsForUser(ServiceUserDetail userDetail) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(userDetail, TeamType.OPRED);
  }

  void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<OpredTeamRole> roles) {
    var rolesAsStrings = getRolesAsStrings(roles);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  @Transactional
  public Team createTeam(String groupName, int organisationGroupId) {
    var team = new Team();
    team.setTeamType(TeamType.OPRED);
    team.setDisplayName(groupName);
    team.setOrganisationGroupId(organisationGroupId);
    teamService.createTeam(team);
    return team;
  }

  public Optional<Team> getTeamByOrganisationGroupId(int organisationGroupId) {
    return teamService.getTeamByOrganisationGroupId(organisationGroupId);
  }

  private Set<String> getRolesAsStrings(Set<OpredTeamRole> opredTeamRoles) {
    return opredTeamRoles.stream()
        .map(OpredTeamRole::name)
        .collect(Collectors.toSet());
  }
}
