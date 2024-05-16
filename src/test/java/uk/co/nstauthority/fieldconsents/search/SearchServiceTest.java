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
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @Mock
  private SearchFilterService searchFilterService;

  @Mock
  private ApplicationDataItemViewService applicationDataItemViewService;

  @InjectMocks
  private SearchService searchService;

  private ServiceUserDetail user;
  private SearchFilterForm form;
  private List<Condition> conditions;
  private List<ApplicationDataItemView> applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
    form = new SearchFilterForm();
    conditions = List.of(mock(Condition.class), mock(Condition.class));
    applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
  }

  @Test
  void getRegulatorApplicationDataItemViews() {
    when(searchFilterService.getConditions(form, TeamType.REGULATOR)).thenReturn(conditions);

    when(applicationDataItemViewService.getRegulatorApplicationDataItems(conditions, user)).thenReturn(
        applicationDataItemViews);

    assertThat(searchService.getRegulatorApplicationDataItemViews(form, user)).isEqualTo(applicationDataItemViews);
  }

  @Test
  void getIndustryApplicationDataItemViews() {
    when(searchFilterService.getConditions(form, TeamType.INDUSTRY)).thenReturn(conditions);

    when(applicationDataItemViewService.getIndustryApplicationDataItems(conditions, user)).thenReturn(
        applicationDataItemViews);

    assertThat(searchService.getIndustryApplicationDataItemViews(form, user)).isEqualTo(applicationDataItemViews);
  }

  @Test
  void getConsulteeApplicationDataItemViews() {
    when(searchFilterService.getConditions(form, TeamType.OPRED)).thenReturn(conditions);

    when(applicationDataItemViewService.getConsulteeApplicationDataItemViews(conditions, user)).thenReturn(
        applicationDataItemViews);

    assertThat(searchService.getConsulteeApplicationDataItemViews(form, user)).isEqualTo(applicationDataItemViews);
  }
}
