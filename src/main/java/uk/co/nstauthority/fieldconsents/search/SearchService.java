package uk.co.nstauthority.fieldconsents.search;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class SearchService {

  private final SearchFilterService searchFilterService;
  private final ApplicationDataItemViewService applicationDataItemViewService;

  SearchService(SearchFilterService searchFilterService, ApplicationDataItemViewService applicationDataItemViewService) {
    this.searchFilterService = searchFilterService;
    this.applicationDataItemViewService = applicationDataItemViewService;
  }

  public List<ApplicationDataItemView> getRegulatorApplicationDataItemViews(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.REGULATOR);

    return applicationDataItemViewService.getRegulatorApplicationDataItems(conditions, user);
  }

  public List<ApplicationDataItemView> getIndustryApplicationDataItemViews(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.INDUSTRY);

    return applicationDataItemViewService.getIndustryApplicationDataItems(conditions, user);
  }

  public List<ApplicationDataItemView> getConsulteeApplicationDataItemViews(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.CONSULTEE);

    return applicationDataItemViewService.getConsulteeApplicationDataItemViews(conditions, user);
  }
}
