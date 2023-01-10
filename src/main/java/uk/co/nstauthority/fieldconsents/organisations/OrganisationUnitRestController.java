package uk.co.nstauthority.fieldconsents.organisations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;

@RestController
public class OrganisationUnitRestController {

  static final String ORG_UNIT_SEARCH_PURPOSE = "Organisation units search selector";

  private final OrganisationUnitService organisationUnitService;

  private final SearchSelectorService searchSelectorService;

  @Autowired
  public OrganisationUnitRestController(OrganisationUnitService organisationUnitService,
                                        SearchSelectorService searchSelectorService) {
    this.organisationUnitService = organisationUnitService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/data-sources/organisation-units")
  public RestSearchResult getOrganisationUnitSearchResults(@RequestParam(value = "term") String term) {

    return searchSelectorService.search(term, searchTerm -> organisationUnitService
        .searchOrganisationUnits(searchTerm, ORG_UNIT_SEARCH_PURPOSE));
  }

}
