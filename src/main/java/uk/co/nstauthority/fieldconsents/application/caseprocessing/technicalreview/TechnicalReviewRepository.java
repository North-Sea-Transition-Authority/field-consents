package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface TechnicalReviewRepository extends CrudRepository<TechnicalReview, Integer> {

  boolean existsByApplicationVersion_ApplicationAndTechnicalReviewStatus(Application application,
                                                                         TechnicalReviewStatus status);

  Optional<TechnicalReview> findByApplicationVersion_ApplicationAndTechnicalReviewStatus(Application application,
                                                                                         TechnicalReviewStatus status);
}
