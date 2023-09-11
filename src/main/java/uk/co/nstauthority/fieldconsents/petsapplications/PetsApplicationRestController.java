package uk.co.nstauthority.fieldconsents.petsapplications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;

@RestController
@AccessibleByServiceUsers
public class PetsApplicationRestController {

  static final String EIA_DIRECTION_SEARCH_PURPOSE = "EIA directions search selector";

  private final PetsApplicationService petsApplicationService;

  private final SearchSelectorService searchSelectorService;

  @Autowired
  public PetsApplicationRestController(PetsApplicationService petsApplicationService,
                                       SearchSelectorService searchSelectorService) {
    this.petsApplicationService = petsApplicationService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/data-sources/eia-directions")
  public RestSearchResult getEiaDirectionSearchResults(@RequestParam(value = "term") String term) {
    return searchSelectorService.search(term, searchTerm -> petsApplicationService
        .searchEiaDirections(searchTerm, EIA_DIRECTION_SEARCH_PURPOSE));
  }
}
