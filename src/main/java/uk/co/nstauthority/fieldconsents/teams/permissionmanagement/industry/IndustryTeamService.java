package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
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
public class IndustryTeamService {

  private final TeamService teamService;
  private final TeamMemberService teamMemberService;
  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  IndustryTeamService(TeamService teamService,
                      TeamMemberService teamMemberService,
                      TeamMemberRoleService teamMemberRoleService) {
    this.teamService = teamService;
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  public boolean isAccessManager(TeamId teamId, ServiceUserDetail user) {
    return teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(IndustryTeamRole.ACCESS_MANAGER.name()));
  }

  Optional<Team> getTeam(TeamId teamId) {
    return teamService.getTeam(teamId, TeamType.INDUSTRY);
  }

  List<Team> getTeamsForUser(ServiceUserDetail userDetail) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(userDetail, TeamType.INDUSTRY);
  }

  void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<IndustryTeamRole> roles) {
    var rolesAsStrings = getRolesAsStrings(roles);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  @Transactional
  public Team createTeam(String groupName, int organisationGroupId) {
    var team = new Team();
    team.setTeamType(TeamType.INDUSTRY);
    team.setDisplayName(groupName);
    team.setOrganisationGroupId(organisationGroupId);
    teamService.createTeam(team);
    return team;
  }

  public Optional<Team> getTeamByOrganisationGroupId(int organisationGroupId) {
    return teamService.getTeamByOrganisationGroupId(organisationGroupId);
  }

  private Set<String> getRolesAsStrings(Set<IndustryTeamRole> industryTeamRoles) {
    return industryTeamRoles.stream()
        .map(IndustryTeamRole::name)
        .collect(Collectors.toSet());
  }
}
