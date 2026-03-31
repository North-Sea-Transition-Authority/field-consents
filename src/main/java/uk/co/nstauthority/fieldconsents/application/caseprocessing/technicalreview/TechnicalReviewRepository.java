package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface TechnicalReviewRepository extends CrudRepository<TechnicalReview, Integer> {

  boolean existsByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(
      Application application,
      TechnicalReviewStatus status
  );

  Optional<TechnicalReview> findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(
      Application application,
      TechnicalReviewStatus status
  );

  List<TechnicalReview> findByRequestApplicationVersion_Application(Application application);

  Optional<TechnicalReview> findByRequestApplicationVersion_ApplicationAndId(
      Application application,
      Integer technicalReviewId
  );

  @Query(
      """
      SELECT DISTINCT tr.technicalReviewerWuaId
      FROM TechnicalReview tr
      WHERE tr.technicalReviewStatus = :status
      """
  )
  List<Long> findAllTechnicalReviewerWuaIdsByTechnicalReviewStatus(TechnicalReviewStatus status);

  List<TechnicalReview> findAllByTechnicalReviewerWuaId(Long technicalReviewerWuaId);
}
