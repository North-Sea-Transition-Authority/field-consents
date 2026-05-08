package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@Service
public class TeamQueryService {

  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final TeamMemberViewQueryService teamMemberViewQueryService;

  TeamQueryService(
      TeamRepository teamRepository,
      TeamRoleRepository teamRoleRepository,
      TeamMemberViewQueryService teamMemberViewQueryService
  ) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.teamMemberViewQueryService = teamMemberViewQueryService;
  }

  public boolean userHasStaticRole(WebUserAccountId webUserAccountId, TeamType teamType, Role role) {
    return userHasAtLeastOneStaticRole(webUserAccountId.id(), teamType, Set.of(role));
  }

  public boolean userHasStaticRole(ServiceUserDetail user, TeamType teamType, Role role) {
    return userHasAtLeastOneStaticRole(user.wuaId(), teamType, Set.of(role));
  }

  public boolean userIsMemberOfTeamType(ServiceUserDetail userDetail, TeamType teamType) {
    return teamRoleRepository.existsByWuaIdAndTeam_TeamType(userDetail.wuaId(), teamType);
  }

  public boolean userHasAtLeastOneStaticRole(ServiceUserDetail userDetail, TeamType teamType, Collection<Role> roles) {
    return userHasAtLeastOneStaticRole(userDetail.wuaId(), teamType, roles);
  }

  private boolean userHasAtLeastOneStaticRole(Long wuaId, TeamType teamType, Collection<Role> roles) {
    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not static".formatted(teamType));
    }

    return teamRepository.findByTeamType(teamType).stream()
        .findFirst()
        .filter(team -> userHasAtLeastOneRole(wuaId, team, roles))
        .isPresent();
  }

  public boolean userHasScopedRole(ServiceUserDetail userDetail, TeamType teamType, TeamScopeReference scopeRef, Role role) {
    return userHasAtLeastOneScopedRole(userDetail, teamType, scopeRef, Set.of(role));
  }

  public boolean userHasAtLeastOneScopedRole(
      ServiceUserDetail userDetail,
      TeamType teamType,
      TeamScopeReference scopeRef,
      Set<Role> roles
  ) {
    if (!teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not scoped".formatted(teamType));
    }
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .filter(team -> userHasAtLeastOneRole(userDetail.wuaId(), team, roles))
        .isPresent();
  }

  public List<TeamRole> getTeamRoles(ServiceUserDetail userDetail) {
    return teamRoleRepository.findAllByWuaId(userDetail.wuaId());
  }

  public List<TeamRole> getTeamRoles(WebUserAccountId webUserAccountId) {
    return teamRoleRepository.findAllByWuaId(webUserAccountId.id());
  }

  public List<TeamRole> getTeamRoles(Team team) {
    return teamRoleRepository.findByTeam(team);
  }

  public List<TeamRole> getTeamRoles(TeamType teamType) {
    return teamRoleRepository.findByTeam_TeamType(teamType);
  }

  public List<TeamRole> getTeamRoles(TeamType teamType, String scopeType, Collection<String> scopeIds) {
    return teamRoleRepository.findAllByTeam_TeamTypeAndTeam_ScopeTypeAndTeam_ScopeIdIn(teamType, scopeType, scopeIds);
  }

  public List<TeamMemberView> getTeamMemberViews(Collection<TeamRole> teamRoles) {
    return teamMemberViewQueryService.getTeamMemberViews(teamRoles);
  }

  public TeamMemberView getTeamMemberView(Team team, Long wuaId) {
    return teamMemberViewQueryService.getTeamMemberView(team, wuaId);
  }

  public Set<Role> getStaticRoles(ServiceUserDetail userDetail, TeamType teamType) {
    return getStaticTeamRoles(userDetail, teamType)
        .stream()
        .map(TeamRole::getRole)
        .collect(Collectors.toSet());
  }

  public List<TeamRole> getStaticTeamRoles(ServiceUserDetail userDetail, TeamType teamType) {
    if (teamType.isScoped()) {
      throw new IllegalArgumentException("Expected non-scoped TeamType. Got %s.".formatted(teamType));
    }

    return teamRoleRepository.findAllByWuaIdAndTeam_TeamType(userDetail.wuaId(), teamType);
  }

  public Team getStaticTeam(TeamType teamType) {
    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not static".formatted(teamType));
    }

    var teams = teamRepository.findByTeamType(teamType);

    if (teams.size() != 1) {
      throw new IllegalArgumentException("Expected exactly one team, got %d".formatted(teams.size()));
    }

    return teams.getFirst();
  }

  public Optional<Team> getIndustryTypeTeamByScopeId(String scopeId) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(
        TeamType.INDUSTRY,
        TeamScopeReference.ORGANISATION_GROUP_ID,
        scopeId
    );
  }

  private boolean userHasAtLeastOneRole(Long wuaId, Team team, Collection<Role> roles) {
    return teamRoleRepository.findByWuaIdAndTeam(wuaId, team)
        .stream()
        .anyMatch(teamRole -> roles.contains(teamRole.getRole()));
  }
}
