package uk.co.nstauthority.fieldconsents.search;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class SearchService {

  private final SearchFilterService searchFilterService;
  private final ApplicationDataItemService applicationDataItemService;

  SearchService(SearchFilterService searchFilterService, ApplicationDataItemService applicationDataItemService) {
    this.searchFilterService = searchFilterService;
    this.applicationDataItemService = applicationDataItemService;
  }

  public List<ApplicationDataItem> getRegulatorApplicationDataItems(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.REGULATOR);

    return applicationDataItemService.getRegulatorApplicationDataItems(conditions, user);
  }

  public List<ApplicationDataItem> getIndustryApplicationDataItems(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.INDUSTRY);

    return applicationDataItemService.getIndustryApplicationDataItems(conditions, user);
  }

  public List<ApplicationDataItem> getConsulteeApplicationDataItems(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.OPRED);

    return applicationDataItemService.getConsulteeApplicationDataItems(conditions, user);
  }
}
