package uk.co.nstauthority.fieldconsents.integrationtest;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("rawtypes")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"development", "integration-test"})
@AutoConfigureFileUploadLibrary
public abstract class AbstractIntegrationTest {

  @Autowired
  protected TransactionTemplate transactionTemplate;

  @Autowired
  protected EntityManager entityManager;

  protected static PostgreSQLContainer fcsDb;

  @DynamicPropertySource
  private static void addProperties(DynamicPropertyRegistry registry) {
    fcsDb = Containers.getOrCreateFcsDb();

    registry.add("database.url", fcsDb::getJdbcUrl);
    registry.add("schema.password", fcsDb::getPassword);
  }

  protected void truncateApplicationsCascade() {
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.createNativeQuery("TRUNCATE TABLE fcs.applications CASCADE").executeUpdate();
      status.flush();
    });
  }
}
