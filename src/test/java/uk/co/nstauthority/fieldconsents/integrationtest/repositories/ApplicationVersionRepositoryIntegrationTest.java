package uk.co.nstauthority.fieldconsents.integrationtest.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;

@Sql(executionPhase = BEFORE_TEST_CLASS, value = "classpath:/scripts/find-submitted-versions-test.sql")
class ApplicationVersionRepositoryIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private ApplicationVersionRepository applicationVersionRepository;

  @Test
  void shouldFindAllWhereLatestVersionIsSubmitted() {
    var number = applicationVersionRepository.countWhereLatestVersionIsSubmittedWithoutCaseOfficer();

    assertThat(number).isEqualTo(3L);
  }
}
