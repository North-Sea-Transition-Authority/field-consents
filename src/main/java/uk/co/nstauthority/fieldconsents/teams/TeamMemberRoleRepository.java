package uk.co.nstauthority.fieldconsents.teams;

import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
interface TeamMemberRoleRepository extends CrudRepository<TeamMemberRole, Integer> {

  List<TeamMemberRole> findAllByTeam(Team team);

  boolean existsByWuaIdAndTeam_Id(long wuaId, Integer teamId);

  boolean existsByWuaIdAndTeam_IdAndRoleIn(long wuaId, Integer teamId, Set<String> roles);

  List<TeamMemberRole> findAllByTeamAndWuaId(Team team, Long wuaId);

  void deleteAllByTeamAndWuaId(Team team, Long wuaId);

  List<TeamMemberRole> findAllByWuaId(long wuaId);

}
