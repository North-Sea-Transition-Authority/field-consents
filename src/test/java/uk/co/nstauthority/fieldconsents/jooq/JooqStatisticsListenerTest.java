package uk.co.nstauthority.fieldconsents.jooq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jooq.ExecuteContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.metrics.QueryCounter;

@ExtendWith(MockitoExtension.class)
class JooqStatisticsListenerTest {

  @Mock
  private QueryCounter queryCounter;

  @InjectMocks
  private JooqStatisticsListener jooqStatisticsListener;

  @Test
  void start() {
    jooqStatisticsListener.start(mock(ExecuteContext.class));
    verify(queryCounter).incrementJooq();
  }
}
