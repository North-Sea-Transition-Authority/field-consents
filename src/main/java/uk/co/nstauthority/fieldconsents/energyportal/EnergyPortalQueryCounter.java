package uk.co.nstauthority.fieldconsents.energyportal;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportalapi.client.QueryListener;
import uk.co.nstauthority.fieldconsents.metrics.QueryCounter;

@Component
public class EnergyPortalQueryCounter implements QueryListener {

  private final QueryCounter queryCounter;

  EnergyPortalQueryCounter(QueryCounter queryCounter) {
    this.queryCounter = queryCounter;
  }

  @Override
  public void onRequest(GraphQLQueryRequest request) {
    queryCounter.incrementEpa();
  }
}
