package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class RegulatorTeamService {

  private final TeamService teamService;

  private final TeamMemberService teamMemberService;

  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  RegulatorTeamService(TeamService teamService, TeamMemberService teamMemberService,
                       TeamMemberRoleService teamMemberRoleService) {
    this.teamService = teamService;
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  public Optional<Team> getRegulatorTeamForUser(ServiceUserDetail user) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.REGULATOR)
        .stream()
        .findFirst();
  }

  Optional<Team> getTeam(TeamId teamId) {
    return teamService.getTeam(teamId, TeamType.REGULATOR);
  }

  boolean isAccessManager(TeamId teamId, ServiceUserDetail user) {
    return teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name()));
  }

  public boolean isCaseOfficer(WebUserAccountId wuaId) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(wuaId, TeamType.REGULATOR)
        .stream()
        .anyMatch(regulatorTeam -> teamMemberService
            .isMemberOfTeamWithAnyRoleOf(regulatorTeam.toTeamId(), wuaId, Set.of(RegulatorTeamRole.CASE_OFFICER.name()))
        );
  }

  public boolean isTechnicalReviewer(WebUserAccountId wuaId) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(wuaId, TeamType.REGULATOR)
        .stream()
        .anyMatch(regulatorTeam -> teamMemberService
            .isMemberOfTeamWithAnyRoleOf(regulatorTeam.toTeamId(), wuaId, Set.of(RegulatorTeamRole.TECHNICAL_REVIEWER.name()))
        );
  }

  void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<RegulatorTeamRole> roles) {
    var rolesAsStrings = roles
        .stream()
        .map(RegulatorTeamRole::name)
        .collect(Collectors.toSet());

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }
}
