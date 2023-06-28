package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface TechnicalReviewRepository extends CrudRepository<TechnicalReview, Integer> {

  boolean existsByApplicationVersionAndTechnicalReviewStatus(ApplicationVersion applicationVersion,
                                                             TechnicalReviewStatus status);

  Optional<TechnicalReview> findByApplicationVersionAndTechnicalReviewStatus(ApplicationVersion applicationVersion,
                                                                             TechnicalReviewStatus status);
}
