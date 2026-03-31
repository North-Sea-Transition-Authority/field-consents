package uk.co.nstauthority.fieldconsents.integrationtest.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;

@Sql(executionPhase = BEFORE_TEST_CLASS, value = "classpath:/scripts/find-submitted-versions-test.sql")
class ApplicationVersionRepositoryIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private ApplicationVersionRepository applicationVersionRepository;

  @Test
  void shouldFindAllWhereLatestVersionIsSubmitted() {
    var results = applicationVersionRepository.findAllWhereLatestVersionIsSubmitted();

    assertThat(results)
        .hasSize(3)
        .extracting(ApplicationVersion::getId)
        .containsExactlyInAnyOrder(10001, 10004, 10005)
        .doesNotContain(10002, 10003, 10006);
  }
}
