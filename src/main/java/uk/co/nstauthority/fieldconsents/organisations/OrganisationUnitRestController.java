package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;
import uk.co.nstauthority.fieldconsents.teams.Role;

@RestController
@AccessibleByServiceUsers
public class OrganisationUnitRestController {

  static final String ORG_UNIT_SEARCH_PURPOSE = "Organisation units search selector";

  static final String ORG_UNIT_WORK_AREA_PURPOSE = "Organisation units available in work-area";

  private final OrganisationUnitSearchService organisationUnitSearchService;
  private final SearchSelectorService searchSelectorService;

  OrganisationUnitRestController(
      OrganisationUnitSearchService organisationUnitSearchService,
      SearchSelectorService searchSelectorService
  ) {
    this.organisationUnitSearchService = organisationUnitSearchService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/data-sources/organisation-units-creator")
  public RestSearchResult getOrganisationUnitsForCreator(@RequestParam(value = "term", required = false) String term,
                                                         ServiceUserDetail user) {
    return searchSelectorService.search(term, searchTerm -> organisationUnitSearchService
        .searchOrganisationUnitsForUser(
            searchTerm,
            ORG_UNIT_SEARCH_PURPOSE,
            user,
            Collections.singleton(Role.CREATOR)
        )
    );
  }

  @GetMapping("/data-sources/organisation-units-viewer")
  public RestSearchResult getOrganisationUnitsForViewer(@RequestParam(value = "term", required = false) String term,
                                                        ServiceUserDetail user) {
    return searchSelectorService.search(term, searchTerm -> organisationUnitSearchService
        .searchOrganisationUnitsForUser(
            searchTerm,
            ORG_UNIT_WORK_AREA_PURPOSE,
            user,
            RoleGroup.union(
                RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES,
                RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES,
                RoleGroup.CONSULTEE_WITH_VIEWER_ROLES
            )
        )
    );
  }
}
