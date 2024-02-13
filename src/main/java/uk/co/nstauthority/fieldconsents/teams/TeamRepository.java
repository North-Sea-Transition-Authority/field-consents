package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
interface TeamRepository extends CrudRepository<Team, Integer> {

  Optional<Team> findByIdAndTeamType(Integer id, TeamType teamType);

  @Query(
      """
      SELECT DISTINCT tmr.team
      FROM TeamMemberRole tmr
      WHERE tmr.wuaId = :wuaId
      AND tmr.team.teamType = :teamType
      """
  )
  List<Team> findAllTeamsOfTypeThatUserIsMemberOf(Long wuaId, TeamType teamType);

  @Query(
      """
      SELECT DISTINCT tmr.team
      FROM TeamMemberRole tmr
      WHERE tmr.wuaId = :wuaId
      """
  )
  List<Team> findAllTeamsThatUserIsMemberOf(Long wuaId);

  List<Team> findAllByTeamTypeIn(Collection<TeamType> teamTypes);

  Optional<Team> findByOrganisationGroupId(int organisationGroupId);

  List<Team> findAllByOrganisationGroupIdIn(Collection<Integer> organisationGroupId);

}
