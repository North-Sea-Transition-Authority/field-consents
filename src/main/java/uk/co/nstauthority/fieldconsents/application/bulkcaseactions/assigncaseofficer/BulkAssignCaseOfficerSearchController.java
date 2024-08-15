package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSelectedApplicationsForm;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("bulk-case-actions/assign-case-officer/search")
@HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
public class BulkAssignCaseOfficerSearchController {

  private final BulkCaseActionService bulkCaseActionService;
  private final BulkAssignCaseOfficerSearchFilterService searchFilterService;

  BulkAssignCaseOfficerSearchController(
      BulkCaseActionService bulkCaseActionService,
      BulkAssignCaseOfficerSearchFilterService searchFilterService
  ) {
    this.bulkCaseActionService = bulkCaseActionService;
    this.searchFilterService = searchFilterService;
  }

  @GetMapping
  public ModelAndView getSearchResults(HttpSession session, ServiceUserDetail user) {
    var sessionContext = BulkAssignCaseOfficerSessionContext.fromSession(session);

    var filtersForm = sessionContext.getSearchFiltersForm();
    var searchConditions = searchFilterService.getConditions(filtersForm, user);
    var applicationDataItemViews = bulkCaseActionService.getApplicationDataItemViews(user, searchConditions);

    var selectedApplicationsForm = sessionContext.getSelectedApplicationsForm();
    selectedApplicationsForm.removeUnavailableApplications(applicationDataItemViews);
    sessionContext.setSelectedApplicationsForm(selectedApplicationsForm);

    var modelAndView = searchResultsModelAndView(applicationDataItemViews, selectedApplicationsForm);
    addSearchFiltersToModelAndView(modelAndView, filtersForm);

    return modelAndView;
  }

  private ModelAndView searchResultsModelAndView(
      List<ApplicationDataItemView> applicationDataItemViews,
      BulkCaseActionSelectedApplicationsForm form
  ) {
    return new ModelAndView("fcs/application/bulk-case-actions/search")
        .addObject("pageTitle", ASSIGN_CASE_OFFICER)
        .addObject("action", ASSIGN_CASE_OFFICER)
        .addObject("applicationDataItemViews", applicationDataItemViews)
        .addObject("form", form);
  }

  private void addSearchFiltersToModelAndView(ModelAndView modelAndView, BulkAssignCaseOfficerSearchFiltersForm filtersForm) {
    var prefilledOperator = searchFilterService.getPrefilledOrganisation(filtersForm.operatorId());
    var prefilledField = searchFilterService.getPrefilledAsset(filtersForm.fieldAssetKey());
    var prefilledTerminal = searchFilterService.getPrefilledAsset(filtersForm.terminalAssetKey());
    var caseOfficerOptions = searchFilterService.getCaseOfficerDisplayOptions();

    var clearFiltersUrl = ReverseRouter.route(on(this.getClass()).clearSearchFilters(null));
    var fieldAssetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null));
    var terminalAssetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null));
    var searchOperatorRestUrl = ReverseRouter.route(on(OrganisationUnitRestController.class)
        .getOrganisationUnitsForViewer(null, null));

    modelAndView
        .addObject("filtersForm", filtersForm)
        .addObject("clearFiltersUrl", clearFiltersUrl)
        .addObject("prefilledOperator", prefilledOperator)
        .addObject("prefilledField", prefilledField)
        .addObject("prefilledTerminal", prefilledTerminal)
        .addObject("fieldAssetSearchRestUrl", fieldAssetSearchRestUrl)
        .addObject("terminalAssetSearchRestUrl", terminalAssetSearchRestUrl)
        .addObject("operatorSearchRestUrl", searchOperatorRestUrl)
        .addObject("geographicAreaCheckboxes", GeographicArea.getDisplayableOptions())
        .addObject("aceCheckboxes", AceFlagStatus.getDisplayableOptions())
        .addObject("assetTypesWithShoreCheckboxes", AssetTypeWithShore.getDisplayableOptions())
        .addObject("caseOfficerOptions", caseOfficerOptions);
  }

  @GetMapping("clear-filters")
  ModelAndView clearSearchFilters(HttpSession session) {
    BulkAssignCaseOfficerSessionContext.fromSession(session).clearFilters();
    return ReverseRouter.redirect(on(this.getClass()).getSearchResults(null, null));
  }

  @PostMapping(params = "Filter results")
  ModelAndView filterSearchResults(@ModelAttribute("form") BulkAssignCaseOfficerSearchFiltersForm form, HttpSession session) {
    BulkAssignCaseOfficerSessionContext.fromSession(session).setFilters(form);
    return ReverseRouter.redirect(on(this.getClass()).getSearchResults(null, null));
  }

  @PostMapping(params = ASSIGN_CASE_OFFICER)
  ModelAndView submitAssignCaseOfficerSelection(
      @Valid @ModelAttribute("form") BulkCaseActionSelectedApplicationsForm form,
      BindingResult bindingResult,
      HttpSession session,
      ServiceUserDetail user
  ) {
    ModelAndView modelAndView;

    if (bindingResult.hasErrors()) {
      modelAndView = getSearchResults(session, user).addObject("form", form);
    } else {
      modelAndView = ReverseRouter.redirect(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null));
    }

    BulkAssignCaseOfficerSessionContext.fromSession(session).setSelectedApplicationsForm(form);
    return modelAndView;
  }
}
