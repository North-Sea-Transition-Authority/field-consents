package uk.co.nstauthority.fieldconsents.organisations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
public class OrganisationUnitRestController {

  static final String ORG_UNIT_SEARCH_PURPOSE = "Organisation units search selector";

  static final String ORG_UNIT_WORK_AREA_PURPOSE = "Organisation units available in work-area";

  private final OrganisationUnitService organisationUnitService;

  private final SearchSelectorService searchSelectorService;

  @Autowired
  public OrganisationUnitRestController(OrganisationUnitService organisationUnitService,
                                        SearchSelectorService searchSelectorService) {
    this.organisationUnitService = organisationUnitService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/data-sources/organisation-units-creator")
  public RestSearchResult getOrganisationUnitsForCreator(@RequestParam(value = "term", required = false) String term,
                                                         ServiceUserDetail user) {
    return searchSelectorService.search(term, searchTerm -> organisationUnitService
        .searchOrganisationUnitsForUser(
            searchTerm,
            ORG_UNIT_SEARCH_PURPOSE,
            user,
            RolePermission.CREATE_FCS_APPLICATIONS
        )
    );
  }

  @GetMapping("/data-sources/organisation-units-editor")
  public RestSearchResult getOrganisationUnitsForEditor(@RequestParam(value = "term", required = false) String term,
                                                        ServiceUserDetail user) {
    return searchSelectorService.search(term, searchTerm -> organisationUnitService
        .searchOrganisationUnitsForUser(
            searchTerm,
            ORG_UNIT_WORK_AREA_PURPOSE,
            user,
            RolePermission.EDIT_FCS_APPLICATIONS
        )
    );
  }
}
