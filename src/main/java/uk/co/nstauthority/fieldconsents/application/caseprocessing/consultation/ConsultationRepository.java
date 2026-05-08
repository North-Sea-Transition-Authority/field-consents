package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;
import uk.co.nstauthority.fieldconsents.teams.Team;

@NotDuplicationSource
interface ConsultationRepository extends CrudRepository<Consultation, Integer> {

  @EntityGraph("consultation")
  Optional<Consultation> findByRequestApplicationVersion_ApplicationAndStatus(
      Application application,
      ConsultationStatus status
  );

  @EntityGraph("consultation")
  List<Consultation> findAllByRequestApplicationVersion_ApplicationOrderById(Application application);

  @EntityGraph("consultation")
  List<Consultation> findAllByRequestApplicationVersion_ApplicationAndConsultationTeamInOrderById(
      Application application,
      Collection<Team> teams
  );

  @EntityGraph("consultation")
  Optional<Consultation> findByIdAndRequestApplicationVersion_Application(Integer id, Application application);

  @EntityGraph("consultation")
  Optional<Consultation> findByIdAndRequestApplicationVersion_ApplicationAndStatus(
      Integer id,
      Application application,
      ConsultationStatus consultationStatus
  );

  @EntityGraph("consultation")
  List<Consultation> findAllByStatus(ConsultationStatus status);

}
