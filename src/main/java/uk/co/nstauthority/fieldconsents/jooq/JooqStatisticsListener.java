package uk.co.nstauthority.fieldconsents.jooq;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.metrics.QueryCounter;

@Component
public class JooqStatisticsListener implements ExecuteListener {

  // This is marked as transient because we don't want to serialise it. `ExecuteListener` is serializable
  private final transient QueryCounter queryCounter;

  JooqStatisticsListener(QueryCounter queryCounter) {
    this.queryCounter = queryCounter;
  }

  @Override
  public void start(ExecuteContext ctx) {
    queryCounter.incrementJooq();
  }

}
