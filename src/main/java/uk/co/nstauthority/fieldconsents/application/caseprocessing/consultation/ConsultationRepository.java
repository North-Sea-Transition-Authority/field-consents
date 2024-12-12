package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;
import uk.co.nstauthority.fieldconsents.teams.Team;

@NotDuplicationSource
interface ConsultationRepository extends CrudRepository<Consultation, Integer> {

  Optional<Consultation> findByRequestApplicationVersion_ApplicationAndStatus(
      Application application,
      ConsultationStatus status
  );

  List<Consultation> findAllByRequestApplicationVersion_ApplicationOrderById(Application application);

  List<Consultation> findAllByRequestApplicationVersion_ApplicationAndConsultationTeamInOrderById(
      Application application,
      Collection<Team> teams
  );

  Optional<Consultation> findByIdAndRequestApplicationVersion_Application(Integer id, Application application);

  Optional<Consultation> findByIdAndRequestApplicationVersion_ApplicationAndStatus(
      Integer id,
      Application application,
      ConsultationStatus consultationStatus
  );

}
