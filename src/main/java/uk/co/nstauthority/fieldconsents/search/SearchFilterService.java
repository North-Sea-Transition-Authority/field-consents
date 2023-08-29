package uk.co.nstauthority.fieldconsents.search;

import java.util.List;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;

@Service
public class SearchFilterService {

  private final ApplicationDataFilterService applicationDataFilterService;

  public SearchFilterService(ApplicationDataFilterService applicationDataFilterService) {
    this.applicationDataFilterService = applicationDataFilterService;
  }

  List<Condition> getConditions(ApplicationDataFilterForm form) {
    return applicationDataFilterService.getConditions(form);
  }

  //TODO: Add search specific conditions when working on other search filters
}
