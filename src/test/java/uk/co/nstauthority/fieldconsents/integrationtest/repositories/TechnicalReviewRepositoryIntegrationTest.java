package uk.co.nstauthority.fieldconsents.integrationtest.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRepository;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;

@Sql(executionPhase = BEFORE_TEST_CLASS, value = "classpath:/scripts/technical-review-entity-graph-test.sql")
class TechnicalReviewRepositoryIntegrationTest extends AbstractIntegrationTest {

  private static final long TECHNICAL_REVIEWER_WUA_ID = 98765;

  private static final int EXPECTED_APPLICATION_NO = 700;

  @Autowired
  private TechnicalReviewRepository technicalReviewRepository;

  @Test
  void entityGraph_whenReadOutsideTransaction_thenRequestApplicationVersionApplicationIsLoadedEagerly() {
    var technicalReviews = technicalReviewRepository
        .findAllByTechnicalReviewerWuaIdAndTechnicalReviewStatus(TECHNICAL_REVIEWER_WUA_ID, OPEN);

    var applicationNo = technicalReviews.getFirst()
        .getRequestApplicationVersion()
        .getApplication()
        .getApplicationNo();

    assertThat(applicationNo).isEqualTo(EXPECTED_APPLICATION_NO);
  }

  @Test
  void entityGraph_whenReadOutsideTransaction_thenResponseApplicationVersionApplicationIsLoadedEagerly() {
    var technicalReviews = technicalReviewRepository
        .findAllByTechnicalReviewerWuaIdAndTechnicalReviewStatus(TECHNICAL_REVIEWER_WUA_ID, CLOSED);

    var applicationNo = technicalReviews.getFirst()
        .getResponseApplicationVersion()
        .getApplication()
        .getApplicationNo();

    assertThat(applicationNo).isEqualTo(EXPECTED_APPLICATION_NO);
  }
}
