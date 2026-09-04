package uk.co.nstauthority.fieldconsents.integrationtest.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.co.nstauthority.fieldconsents.application.ApplicationRepository;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;

@Sql(executionPhase = BEFORE_TEST_CLASS, value = "classpath:/scripts/active-revision-application-exists-test.sql")
class ApplicationRepositoryIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private ApplicationRepository applicationRepository;

  @ParameterizedTest(name = "{0}")
  @CsvSource({
      "no revision application,                                      20000, false",
      "revision application is submitted,                            20001, true",
      "revision application is withdrawn,                            20003, false",
      "revision application is closed,                               20005, false",
      "revision application is deleted,                              20007, false",
      "submitted revision application with a deleted update version, 20009, true",
      "closed revision application with a deleted update version,    20011, false",
      "queried from the revision application itself,                 20002, false"
  })
  void activeRevisionApplicationExists(String scenario, int applicationId, boolean expectedToExist) {
    var application = applicationRepository.findById(applicationId).orElseThrow();

    assertThat(applicationRepository.activeRevisionApplicationExists(application))
        .as(scenario)
        .isEqualTo(expectedToExist);
  }
}
