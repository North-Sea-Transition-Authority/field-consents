package uk.co.nstauthority.fieldconsents.search;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;

@Service
public class SearchFilterFormService {

  public static final String ORGANISATION_UNIT_LOOKUP_PURPOSE = "Lookup organisation unit for search data items";

  private final ApplicationDataFilterFormService applicationDataFilterFormService;

  public SearchFilterFormService(ApplicationDataFilterFormService applicationDataFilterFormService) {
    this.applicationDataFilterFormService = applicationDataFilterFormService;
  }

  public RestSearchItem getPrefilledOrganisation(Integer operatorId) {
    return applicationDataFilterFormService.getPrefilledOrganisation(operatorId, ORGANISATION_UNIT_LOOKUP_PURPOSE);
  }
}
