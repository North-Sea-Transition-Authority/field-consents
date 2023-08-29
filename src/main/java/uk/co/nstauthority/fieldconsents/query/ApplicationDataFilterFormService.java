package uk.co.nstauthority.fieldconsents.query;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class ApplicationDataFilterFormService {

  private final OrganisationUnitService organisationUnitService;

  public ApplicationDataFilterFormService(OrganisationUnitService organisationUnitService) {
    this.organisationUnitService = organisationUnitService;
  }

  public RestSearchItem getPrefilledOrganisation(Integer operatorId, String requestPurpose) {
    if (operatorId == null) {
      return RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    }

    return organisationUnitService.findOrganisationUnitById(operatorId, requestPurpose)
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
