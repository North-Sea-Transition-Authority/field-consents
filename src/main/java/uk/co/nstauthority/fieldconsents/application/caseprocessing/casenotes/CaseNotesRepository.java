package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface CaseNotesRepository extends CrudRepository<CaseNote, Integer> {

  List<CaseNote> findByApplicationVersion_Application(Application application);

  Optional<CaseNote> findByIdAndApplicationVersion_Application(Integer id, Application application);

}
