package uk.co.nstauthority.fieldconsents.integrationtest;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Containers {
  private static final Network NETWORK = Network.newNetwork();
  private static PostgreSQLContainer FCS_DB;

  public static PostgreSQLContainer getOrCreateFcsDb() {
    if (FCS_DB == null) {
      try {
        startUp();
      } catch (Throwable t) {
        throw new Error(t);
      }
    }
    return FCS_DB;
  }

  private static PostgreSQLContainer startUpDbContainer(Slf4jLogConsumer logConsumer) {
    PostgreSQLContainer dbContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:14.3-alpine"))
        .withDatabaseName("fcs")
        .withUsername("fcs_app");
    dbContainer.withNetwork(Containers.NETWORK).withNetworkAliases("database");
    dbContainer.start();
    dbContainer.followOutput(logConsumer);
    return dbContainer;
  }

  private static void startUp() {

    Slf4jLogConsumer dbLogConsumer = new Slf4jLogConsumer(LoggerFactory.getLogger("Postgres"));
    FCS_DB = startUpDbContainer(dbLogConsumer);
  }
}
