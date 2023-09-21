package uk.co.nstauthority.fieldconsents.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/search")
@SessionAttributes({"searchSession"})
@HasPermission(permissions = {RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS})
public class SearchController {

  public static final String SEARCH_TITLE = "Search";

  static final String SEARCH_RESULT_ITEMS = "searchResultItems";

  private final TeamService teamService;

  private final SearchService searchService;

  private final ApplicationDataFilterFormService applicationDataFilterFormService;

  SearchController(TeamService teamService,
                   SearchService searchService,
                   ApplicationDataFilterFormService applicationDataFilterFormService) {
    this.teamService = teamService;
    this.searchService = searchService;
    this.applicationDataFilterFormService = applicationDataFilterFormService;
  }

  @GetMapping
  public ModelAndView getSearch(@ModelAttribute("searchSession") SearchSession searchSession,
                                ServiceUserDetail user) {
    if (searchSession.hasSearchBeenInvoked()) {
      return getSearchModelAndView(searchSession)
          .addObject(SEARCH_RESULT_ITEMS, getSearchResultItems(searchSession, user));
    }
    return getSearchModelAndView(searchSession);
  }

  private ModelAndView getSearchModelAndView(SearchSession searchSession) {
    var appStatuses = ApplicationVersionStatus.getSearchOptions();
    var appTypes = ApplicationType.getDisplayableOptions();
    var durationTypes = ConsentLengthType.getConsentLengthOptions();
    var searchFilterForm = searchSession.getSearchFilterForm();
    var prefilledField = applicationDataFilterFormService.getPrefilledAsset(searchFilterForm.getFieldAssetKey());
    var prefilledTerminal = applicationDataFilterFormService.getPrefilledAsset(searchFilterForm.getTerminalAssetKey());
    var prefilledOperator = applicationDataFilterFormService.getPrefilledOrganisation(searchFilterForm.getOperatorId());
    var assetTypesWithShore = AssetTypeWithShore.getDisplayableOptions();
    var aceStatuses = AceFlagStatus.getDisplayableOptions();
    return new ModelAndView("fcs/search/search")
        .addObject("clearFiltersUrl",
            ReverseRouter.route(on(SearchController.class).clearSearchFilter(null, null)))
        .addObject("appStatuses", appStatuses)
        .addObject("aceStatuses", aceStatuses)
        .addObject("appTypes", appTypes)
        .addObject("durationTypes", durationTypes)
        .addObject("prefilledOperator", prefilledOperator)
        .addObject("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .addObject("prefilledField", prefilledField)
        .addObject("fieldAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null)))
        .addObject("prefilledTerminal", prefilledTerminal)
        .addObject("terminalAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null)))
        .addObject("assetTypesWithShore", assetTypesWithShore)
        .addObject("form", searchFilterForm)
        .addObject("pageTitle", SEARCH_TITLE)
        .addObject("searchInvoked", searchSession.hasSearchBeenInvoked());
  }

  @PostMapping
  ModelAndView searchApplications(@ModelAttribute("form") SearchFilterForm form,
                                  @ModelAttribute("searchSession") SearchSession searchSession) {
    searchSession.update(form);
    return ReverseRouter.redirect(on(SearchController.class).getSearch(null, null));
  }

  private List<SearchResultItem> getSearchResultItems(SearchSession searchSession, ServiceUserDetail user) {
    if (teamService.isRegulatorUser(user)) {
      return searchService.getRegulatorSearchResultItems(searchSession.getSearchFilterForm(), user);
    }

    if (teamService.isIndustryUser(user)) {
      return searchService.getIndustrySearchResultItems(searchSession.getSearchFilterForm(), user);
    }

    return Collections.emptyList();
  }

  @GetMapping("/clear-filters")
  public ModelAndView clearSearchFilter(@ModelAttribute("searchSession") SearchSession searchSession,
                                        SessionStatus sessionStatus) {
    sessionStatus.setComplete();
    searchSession.clearSession();
    return ReverseRouter.redirect(on(SearchController.class).getSearch(null, null));
  }

  @ModelAttribute("searchSession")
  private SearchSession getSearchSession(@ModelAttribute("form") SearchFilterForm searchFilterForm) {
    return new SearchSession(searchFilterForm);
  }
}
