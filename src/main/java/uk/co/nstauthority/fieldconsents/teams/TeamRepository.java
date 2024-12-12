package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface TeamRepository extends ListCrudRepository<Team, UUID> {

  List<Team> findByTeamType(TeamType teamType);

  Optional<Team> findByTeamTypeAndScopeTypeAndScopeId(TeamType teamType, String scopeType, String scopeId);

  List<Team> findAllByTeamTypeAndScopeTypeAndScopeIdIn(TeamType teamType, String scopeType, Collection<String> scopeIds);

}
