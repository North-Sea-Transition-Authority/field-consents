package uk.co.nstauthority.fieldconsents.integrationtest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("rawtypes")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"development", "integration-test"})
@AutoConfigureFileUploadLibrary
public abstract class AbstractIntegrationTest {
  protected static PostgreSQLContainer fcsDb;

  @DynamicPropertySource
  private static void addProperties(DynamicPropertyRegistry registry) {
    fcsDb = Containers.getOrCreateFcsDb();

    registry.add("database.url", fcsDb::getJdbcUrl);
    registry.add("schema.password", fcsDb::getPassword);
  }

  @Bean
  Clock clock() {
    return Clock.fixed(Instant.now(), ZoneId.systemDefault());
  }
}
