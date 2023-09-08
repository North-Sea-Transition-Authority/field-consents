package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;

@ExtendWith(MockitoExtension.class)
class SearchFilterServiceTest {

  @Mock
  private DSLContext context;
  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @InjectMocks
  private SearchFilterService searchFilterService;

  @Test
  void getConditions_withEmptyFilter() {
    assertThat(searchFilterService.getConditions(new SearchFilterForm())).isEmpty();
  }

  // TODO: Add tests for other filters as integration tests (see FCS-440)
}
