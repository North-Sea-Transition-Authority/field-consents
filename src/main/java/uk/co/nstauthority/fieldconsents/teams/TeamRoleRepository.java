package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface TeamRoleRepository extends ListCrudRepository<TeamRole, UUID> {

  List<TeamRole> findByWuaIdAndRole(Long wuaId, Role role);

  List<TeamRole> findByWuaIdAndTeam(Long wuaId, Team team);

  List<TeamRole> findByTeam(Team team);

  List<TeamRole> findByTeam_TeamType(TeamType teamType);

  void deleteByWuaIdAndTeam(Long wuaId, Team team);

  boolean existsByTeamAndWuaId(Team team, Long wuaId);

  List<TeamRole> findAllByWuaId(long wuaId);

  boolean existsByWuaIdAndTeam_TeamType(Long wuaId, TeamType teamType);

  List<TeamRole> findAllByWuaIdAndTeam_TeamType(Long wuaId, TeamType teamType);

  List<TeamRole> findAllByTeam_TeamTypeAndTeam_ScopeTypeAndTeam_ScopeIdIn(
      TeamType teamType,
      String scopeType,
      Collection<String> scopeIds
  );

  Set<TeamRole> findDistinctByWuaIdAndRoleInAndTeam_teamType(
      Long wuaId,
      Collection<Role> roles,
      TeamType teamType
  );
}
