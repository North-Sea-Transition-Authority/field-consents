package uk.co.nstauthority.fieldconsents.application.eiadirection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@Service
class EiaDirectionFormService {

  static final String EIA_DIRECTION_SEARCH_PURPOSE = "Get preselected eia screening direction reference";

  private final PetsApplicationService petsApplicationService;

  @Autowired
  EiaDirectionFormService(PetsApplicationService petsApplicationService) {
    this.petsApplicationService = petsApplicationService;
  }

  RestSearchItem getPrefilledEiaDirectionRef(Integer satId) {
    if (satId == null) {
      return RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    }

    return petsApplicationService.findPetsApplicationById(satId, EIA_DIRECTION_SEARCH_PURPOSE)
        .map(petsApplicationJson ->
            new RestSearchItem(petsApplicationJson.getSelectionId(), petsApplicationJson.getSelectionText()))
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
