package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface CaseNotesRepository extends CrudRepository<CaseNote, Integer> {
}
