package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface FlareRepository extends CrudRepository<Flare, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<Flare> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  List<Flare> findAllByApplicationVersionOrderByIdAsc(ApplicationVersion applicationVersion);

  Optional<Flare> findByApplicationVersionAndFlareNo(ApplicationVersion applicationVersion, Integer flareNo);
}
