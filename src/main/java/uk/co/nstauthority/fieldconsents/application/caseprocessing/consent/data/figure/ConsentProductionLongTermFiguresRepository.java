package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface ConsentProductionLongTermFiguresRepository
    extends ListCrudRepository<ConsentProductionLongTermFigures, Integer> {

  List<ConsentProductionLongTermFigures> findAllByApplication(Application application);

  void deleteAllByApplication(Application application);
}
