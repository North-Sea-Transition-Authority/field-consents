package uk.co.nstauthority.fieldconsents.integrationtest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("rawtypes")
@IntegrationTest
@ExtendWith(SpringExtension.class)
public abstract class AbstractIntegrationTest {
  protected static PostgreSQLContainer fcsDb;

  @DynamicPropertySource
  private static void addProperties(DynamicPropertyRegistry registry) {
    fcsDb = Containers.getOrCreateFcsDb();

    registry.add("database.url", fcsDb::getJdbcUrl);
    registry.add("schema.password", fcsDb::getPassword);
  }
}
