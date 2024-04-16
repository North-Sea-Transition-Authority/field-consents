package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @Mock
  private SearchFilterService searchFilterService;

  @Mock
  private ApplicationDataItemService applicationDataItemService;

  @InjectMocks
  private SearchService searchService;

  private ServiceUserDetail user;
  private SearchFilterForm form;
  private List<Condition> conditions;
  private List<ApplicationDataItem> applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
    form = new SearchFilterForm();
    conditions = List.of(mock(Condition.class), mock(Condition.class));
    applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());
  }

  @Test
  void getRegulatorApplicationDataItems() {
    when(searchFilterService.getConditions(form, TeamType.REGULATOR)).thenReturn(conditions);

    when(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user)).thenReturn(applicationDataItems);

    assertThat(searchService.getRegulatorApplicationDataItems(form, user)).isEqualTo(applicationDataItems);
  }

  @Test
  void getIndustryApplicationDataItems() {
    when(searchFilterService.getConditions(form, TeamType.INDUSTRY)).thenReturn(conditions);

    when(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).thenReturn(applicationDataItems);

    assertThat(searchService.getIndustryApplicationDataItems(form, user)).isEqualTo(applicationDataItems);
  }

  @Test
  void getConsulteeApplicationDataItems() {
    when(searchFilterService.getConditions(form, TeamType.OPRED)).thenReturn(conditions);

    when(applicationDataItemService.getConsulteeApplicationDataItems(conditions, user)).thenReturn(applicationDataItems);

    assertThat(searchService.getConsulteeApplicationDataItems(form, user)).isEqualTo(applicationDataItems);
  }
}
