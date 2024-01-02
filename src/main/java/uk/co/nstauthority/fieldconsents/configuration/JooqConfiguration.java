package uk.co.nstauthority.fieldconsents.configuration;

import javax.sql.DataSource;
import org.jooq.ConnectionProvider;
import org.jooq.ExecuteListenerProvider;
import org.jooq.TransactionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.boot.autoconfigure.jooq.JooqProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.nstauthority.fieldconsents.jooq.JooqStatisticsListener;

@Configuration
public class JooqConfiguration {

  /*
  The below configuration takes the default configuration found here:
  org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration#jooqConfiguration
  and adds our own custom statistics listener.
   */
  @Bean
  public DefaultConfiguration configuration(
      JooqProperties properties,
      ConnectionProvider connectionProvider,
      DataSource dataSource,
      ObjectProvider<TransactionProvider> transactionProvider,
      ObjectProvider<ExecuteListenerProvider> executeListenerProviders,
      ObjectProvider<DefaultConfigurationCustomizer> configurationCustomizers,
      JooqStatisticsListener jooqStatisticsListener
  ) {
    var configuration = new DefaultConfiguration();
    configuration.set(properties.determineSqlDialect(dataSource));
    configuration.set(connectionProvider);
    transactionProvider.ifAvailable(configuration::set);
    configuration.set(executeListenerProviders.orderedStream().toArray(ExecuteListenerProvider[]::new));
    configurationCustomizers.orderedStream().forEach(customizer -> customizer.customize(configuration));

    // custom statistics listener
    configuration.set(new DefaultExecuteListenerProvider(jooqStatisticsListener));
    return configuration;
  }

}
