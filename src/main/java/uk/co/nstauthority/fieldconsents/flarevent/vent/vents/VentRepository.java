package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface VentRepository extends CrudRepository<Vent, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<Vent> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  List<Vent> findAllByApplicationVersionOrderByIdAsc(ApplicationVersion applicationVersion);

  Optional<Vent> findByApplicationVersionAndVentNo(ApplicationVersion applicationVersion, Integer ventNo);
}
