package uk.co.nstauthority.fieldconsents.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm.APPROVED_FOR_ISSUE_FILTER_OPTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Controller
@RequestMapping("/search")
@SessionAttributes({"searchSession"})
@AccessibleByServiceUsers
public class SearchController {

  public static final String SEARCH_TITLE = "Search";

  public static final String SEARCH_RESULT_ITEMS = "searchResultItems";

  private final TeamService teamService;

  private final SearchService searchService;

  private final ApplicationDataFilterFormService applicationDataFilterFormService;

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

  private static final int SEARCH_RESULT_RENDER_LIMIT = 300;

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
      List<ApplicationDataItemView> results = getApplicationDataItemViews(searchSession, user);

      return getSearchModelAndView(searchSession, user)
          .addObject(SEARCH_RESULT_ITEMS, results.stream().limit(SEARCH_RESULT_RENDER_LIMIT).toList())
          .addObject("searchResultsLimited", results.size() > SEARCH_RESULT_RENDER_LIMIT);
    }
    return getSearchModelAndView(searchSession, user);
  }

  private ModelAndView getSearchModelAndView(SearchSession searchSession, ServiceUserDetail user) {
    var appStatuses = ApplicationVersionStatus.getSearchOptions();
    var appTypes = ApplicationType.getDisplayableOptions();
    var durationTypes = ConsentLengthType.getConsentLengthOptions();
    var searchFilterForm = searchSession.getSearchFilterForm();
    var prefilledField = applicationDataFilterFormService.getPrefilledAsset(searchFilterForm.getFieldAssetKey());
    var prefilledTerminal = applicationDataFilterFormService.getPrefilledAsset(searchFilterForm.getTerminalAssetKey());
    var prefilledOperator = applicationDataFilterFormService.getPrefilledOrganisation(searchFilterForm.getOperatorId());
    var assetTypesWithShore = AssetTypeWithShore.getDisplayableOptions();
    var isRegulator = teamService.isRegulatorUser(user);
    var isRegulatorOrConsultee = isRegulator || teamService.isConsulteeUser(user);
    var modelAndView = new ModelAndView("fcs/search/search")
        .addObject("clearFiltersUrl",
            ReverseRouter.route(on(SearchController.class).clearSearchFilter(null, null)))
        .addObject("appStatuses", appStatuses)
        .addObject("appTypes", appTypes)
        .addObject("durationTypes", durationTypes)
        .addObject("prefilledOperator", prefilledOperator)
        .addObject("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .addObject("prefilledField", prefilledField)
        .addObject("fieldAssetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchFieldAssetsForUser(null, null)))
        .addObject("prefilledTerminal", prefilledTerminal)
        .addObject("terminalAssetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchTerminalAssetsForUser(null, null)))
        .addObject("assetTypesWithShore", assetTypesWithShore)
        .addObject("form", searchFilterForm)
        .addObject("pageTitle", SEARCH_TITLE)
        .addObject("searchInvoked", searchSession.hasSearchBeenInvoked());

    if (isRegulator) {
      modelAndView.addObject("approvedForIssue",
          Map.of(APPROVED_FOR_ISSUE_FILTER_OPTION, Boolean.TRUE.equals(searchFilterForm.getApprovedForIssue())));
    }

    if (isRegulatorOrConsultee) {
      modelAndView.addObject("aceStatuses", AceFlagStatus.getDisplayableOptions());
    }

    return modelAndView;
  }

  @PostMapping
  ModelAndView searchApplications(@ModelAttribute("form") SearchFilterForm form,
                                  @ModelAttribute("searchSession") SearchSession searchSession) {
    searchSession.update(form);
    return ReverseRouter.redirect(on(SearchController.class).getSearch(null, null));
  }

  private List<ApplicationDataItemView> getApplicationDataItemViews(SearchSession searchSession, ServiceUserDetail user) {
    List<ApplicationDataItemView> results = new ArrayList<>();

    if (teamService.isRegulatorUser(user)) {
      LOGGER.info("Starting Search [Regulator] with filters: {}", searchSession.getSearchFilterForm().prettyPrint());
      results = searchService.getRegulatorApplicationDataItemViews(searchSession.getSearchFilterForm(), user);
    }

    if (teamService.isIndustryUser(user)) {
      LOGGER.info("Starting Search [Industry] with filters: {}", searchSession.getSearchFilterForm().prettyPrint());
      results = searchService.getIndustryApplicationDataItemViews(searchSession.getSearchFilterForm(), user);
    }

    if (teamService.isConsulteeUser(user)) {
      LOGGER.info("Starting Search [Consultee] with filters: {}", searchSession.getSearchFilterForm().prettyPrint());
      results = searchService.getConsulteeApplicationDataItemViews(searchSession.getSearchFilterForm(), user);
    }

    LOGGER.info("Search completed with {} items", results.size());

    return results;
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
