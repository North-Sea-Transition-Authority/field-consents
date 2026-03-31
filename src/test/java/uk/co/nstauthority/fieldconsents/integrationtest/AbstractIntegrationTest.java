package uk.co.nstauthority.fieldconsents.integrationtest;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import uk.co.nstauthority.fieldconsents.configuration.EnergyPortalMessageQueueTestConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"development", "integration-test"})
@AutoConfigureFileUploadLibrary
@Import(EnergyPortalMessageQueueTestConfiguration.class)
@DirtiesContext(classMode = AFTER_CLASS)
public abstract class AbstractIntegrationTest {

  @Autowired
  protected TransactionTemplate transactionTemplate;

  @Autowired
  protected EntityManager entityManager;

  protected static PostgreSQLContainer<?> fcsDb;

  @SuppressWarnings("resource")
  @DynamicPropertySource
  private static void addProperties(DynamicPropertyRegistry registry) {
    fcsDb = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.3-alpine"))
        .withDatabaseName("fcs")
        .withUsername("fcs_app")
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Postgres")));
    fcsDb.start();

    registry.add("database.url", fcsDb::getJdbcUrl);
    registry.add("schema.password", fcsDb::getPassword);
  }

  @AfterAll
  static void stopContainer() {
    if (fcsDb != null) {
      fcsDb.stop();
      fcsDb = null;
    }
  }

  protected void truncateApplicationsCascade() {
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.createNativeQuery("TRUNCATE TABLE fcs.applications CASCADE").executeUpdate();
      status.flush();
    });
  }
}
