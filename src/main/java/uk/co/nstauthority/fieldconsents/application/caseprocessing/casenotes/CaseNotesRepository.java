package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseNotesRepository extends CrudRepository<CaseNote, Integer> {
}
