package uk.co.nstauthority.fieldconsents.energyportal.organisationgroup;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api")
@AccessibleByServiceUsers
public class OrganisationGroupRestController {
  private final OrganisationGroupQueryService organisationGroupQueryService;
  private final SearchSelectorService searchSelectorService;

  public OrganisationGroupRestController(OrganisationGroupQueryService organisationGroupQueryService,
                                         SearchSelectorService searchSelectorService) {
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/organisation-group")
  public RestSearchResult getOrganisationGroupSearchResults(@RequestParam(value = "term", required = false) String term) {
    return searchSelectorService.search(term, organisationGroupQueryService::getOrganisationGroupsByName);
  }
}
