package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface ConsentDataLongTermEmissionFiguresRepository
    extends ListCrudRepository<ConsentDataLongTermEmissionFigures, Integer> {

  List<ConsentDataLongTermEmissionFigures> findAllByApplication(Application application);
}
