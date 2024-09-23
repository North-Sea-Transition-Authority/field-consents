package uk.co.nstauthority.fieldconsents.search;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@Service
public class SearchFilterFormService {

  private final OrganisationGroupQueryService organisationGroupQueryService;

  public SearchFilterFormService(OrganisationGroupQueryService organisationGroupQueryService) {
    this.organisationGroupQueryService = organisationGroupQueryService;
  }

  public RestSearchItem getPrefilledOrganisationGroup(Integer organisationGroupId) {
    if (organisationGroupId == null) {
      return RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    }

    return organisationGroupQueryService.getOrganisationGroupById(organisationGroupId)
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
