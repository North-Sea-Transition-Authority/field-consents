package uk.co.nstauthority.fieldconsents.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@HasPermission(permissions = {RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS})
public class SearchController {

  public static final String SEARCH_TITLE = "Search";

  private static final String SEARCH_RESULT_ITEMS = "searchResultItems";

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
  public ModelAndView getSearch(@ModelAttribute("form") SearchFilterForm form) {
    return getSearchModelAndView(form);
  }

  private ModelAndView getSearchModelAndView(SearchFilterForm form) {
    var appStatuses = ApplicationVersionStatus.getSearchOptions();
    var appTypes = ApplicationType.getDisplayableOptions();
    var durationTypes = ConsentLengthType.getConsentLengthOptions();
    var prefilledField = applicationDataFilterFormService.getPrefilledAsset(form.getFieldAssetKey());
    var prefilledOperator = applicationDataFilterFormService.getPrefilledOrganisation(form.getOperatorId());
    var assetTypesWithShore = AssetTypeWithShore.getDisplayableOptions();
    var aceStatuses = AceFlagStatus.getDisplayableOptions();

    return new ModelAndView("fcs/search/search")
        .addObject("clearFiltersUrl",
            ReverseRouter.route(on(SearchController.class).clearSearchFilter(null)))
        .addObject("appStatuses", appStatuses)
        .addObject("aceStatuses", aceStatuses)
        .addObject("appTypes", appTypes)
        .addObject("durationTypes", durationTypes)
        .addObject("prefilledOperator", prefilledOperator)
        .addObject("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .addObject("prefilledField", prefilledField)
        .addObject("fieldAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null)))
        .addObject("assetTypesWithShore", assetTypesWithShore)
        .addObject("form", form)
        .addObject("pageTitle", SEARCH_TITLE);
  }

  @PostMapping
  ModelAndView searchApplications(@ModelAttribute("form") SearchFilterForm form,
                                  ServiceUserDetail user) {

    var modelAndView = getSearchModelAndView(form)
        .addObject("showResults", true);

    if (teamService.isRegulatorUser(user)) {
      return modelAndView.addObject(SEARCH_RESULT_ITEMS, searchService.getRegulatorSearchResultItems(form, user));
    }

    if (teamService.isIndustryUser(user)) {
      return modelAndView.addObject(SEARCH_RESULT_ITEMS, searchService.getIndustrySearchResultItems(form, user));
    }

    return modelAndView.addObject(SEARCH_RESULT_ITEMS, Collections.emptyList());
  }

  @GetMapping("/clear-filters")
  public ModelAndView clearSearchFilter(@ModelAttribute("form") SearchFilterForm form) {
    form.clearFilter();
    return ReverseRouter.redirect(on(SearchController.class).getSearch(null));
  }
}
