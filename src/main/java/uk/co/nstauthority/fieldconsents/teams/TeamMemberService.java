package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class TeamMemberService {

  private final TeamMemberRoleRepository teamMemberRoleRepository;

  @Autowired
  public TeamMemberService(TeamMemberRoleRepository teamMemberRoleRepository) {
    this.teamMemberRoleRepository = teamMemberRoleRepository;
  }

  public List<TeamMember> getTeamMembers(Team team) {

    // Group all roles based on the web user account id
    Map<WebUserAccountId, List<TeamMemberRole>> wuaIdToRolesMap = teamMemberRoleRepository.findAllByTeam(team)
        .stream()
        .collect(Collectors.groupingBy(teamMemberRole -> new WebUserAccountId(teamMemberRole.getWuaId())));

    // Convert to a list of TeamMember objects.
    return wuaIdToRolesMap.entrySet()
        .stream()
        .map(entry -> new TeamMember(entry.getKey(), createTeamView(team),
            mapMemberRolesToTeamRoles(entry.getValue(), team)))
        .toList();
  }

  public Optional<TeamMember> getTeamMember(Team team, WebUserAccountId wuaId) {
    // Group all roles to the appropriate wuaId
    var teamMemberRoles = teamMemberRoleRepository.findAllByTeamAndWuaId(team, wuaId.id());

    if (teamMemberRoles.isEmpty()) {
      return Optional.empty();
    }

    var teamMember = new TeamMember(wuaId, createTeamView(team), mapMemberRolesToTeamRoles(teamMemberRoles, team));
    return Optional.of(teamMember);
  }

  private TeamView createTeamView(Team team) {
    return new TeamView(new TeamId(team.getId()), team.getTeamType(), team.getDisplayName());
  }

  public boolean isMemberOfTeam(TeamId teamId, ServiceUserDetail user) {
    return teamMemberRoleRepository.existsByWuaIdAndTeam_Id(user.wuaId(), teamId.id());
  }

  public boolean isMemberOfTeam(TeamId teamId, WebUserAccountId webUserAccountId) {
    return teamMemberRoleRepository.existsByWuaIdAndTeam_Id(webUserAccountId.id(), teamId.id());
  }

  public boolean isMemberOfTeamWithAnyRoleOf(TeamId teamId, ServiceUserDetail user, Set<String> roles) {
    return teamMemberRoleRepository.existsByWuaIdAndTeam_IdAndRoleIn(user.wuaId(), teamId.id(), roles);
  }

  public boolean isMemberOfTeamWithAnyRoleOf(TeamId teamId, WebUserAccountId wuaId, Set<String> roles) {
    return teamMemberRoleRepository.existsByWuaIdAndTeam_IdAndRoleIn(wuaId.id(), teamId.id(), roles);
  }

  public List<TeamMember> getUserAsTeamMembers(ServiceUserDetail user) {
    Map<Team, List<TeamMemberRole>> teamRoleMap = teamMemberRoleRepository.findAllByWuaId(user.wuaId())
        .stream()
        .collect(Collectors.groupingBy(TeamMemberRole::getTeam));

    return teamRoleMap.entrySet()
        .stream()
        .map(entry -> new TeamMember(
            new WebUserAccountId(user.wuaId()),
            createTeamView(entry.getKey()),
            mapMemberRolesToTeamRoles(entry.getValue(), entry.getKey())
        ))
        .toList();
  }

  private Set<TeamRole> mapMemberRolesToTeamRoles(Collection<TeamMemberRole> roles, Team team) {
    return roles.stream()
        .map(teamMemberRole -> switch (team.getTeamType()) {
          case REGULATOR -> RegulatorTeamRole.valueOf(teamMemberRole.getRole());
          case INDUSTRY -> IndustryTeamRole.valueOf(teamMemberRole.getRole());
        })
        .collect(Collectors.toSet());
  }

}
