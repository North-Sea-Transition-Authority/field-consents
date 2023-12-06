package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

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
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Controller
@RequestMapping("bulk-case-actions/search")
@HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
public class BulkCaseActionSearchController {

  public static final String PAGE_TITLE = "Bulk case actions";

  private final BulkCaseActionService bulkCaseActionService;
  private final BulkCaseActionControllerHelperService controllerHelperService;
  private final BulkCaseActionSearchFilterService searchFilterService;
  private final CaseAssignmentService caseAssignmentService;

  BulkCaseActionSearchController(
      BulkCaseActionService bulkCaseActionService,
      BulkCaseActionControllerHelperService controllerHelperService,
      BulkCaseActionSearchFilterService searchFilterService,
      CaseAssignmentService caseAssignmentService
  ) {
    this.bulkCaseActionService = bulkCaseActionService;
    this.controllerHelperService = controllerHelperService;
    this.searchFilterService = searchFilterService;
    this.caseAssignmentService = caseAssignmentService;
  }

  @GetMapping
  public ModelAndView getSearchResults(HttpSession session, ServiceUserDetail user) {
    var filtersForm = controllerHelperService.getSearchFiltersForm(session);
    var searchConditions = searchFilterService.getConditions(filtersForm, user);
    var applicationDataItems = bulkCaseActionService.getApplicationDataItems(user, searchConditions);
    var form = controllerHelperService.getSelectedApplicationsForm(session, applicationDataItems);

    var modelAndView = searchResultsModelAndView(applicationDataItems, form);
    addSearchFiltersToModelAndView(modelAndView, filtersForm);

    return modelAndView;
  }

  private ModelAndView searchResultsModelAndView(
      List<ApplicationDataItem> applicationDataItems,
      BulkCaseActionSelectedApplicationsForm form
  ) {
    return new ModelAndView("fcs/application/bulk-case-actions/search")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("actions", bulkCaseActionService.getBulkActions())
        .addObject("applicationDataItems", applicationDataItems)
        .addObject("form", form);
  }

  private void addSearchFiltersToModelAndView(ModelAndView modelAndView, BulkCaseActionSearchFiltersForm filtersForm) {
    var prefilledOperator = searchFilterService.getPrefilledOrganisation(filtersForm.operatorId());
    var prefilledField = searchFilterService.getPrefilledAsset(filtersForm.fieldAssetKey());
    var prefilledTerminal = searchFilterService.getPrefilledAsset(filtersForm.terminalAssetKey());
    var caseOfficerOptions = caseAssignmentService.getCurrentCaseOfficers()
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            energyPortalUserDto -> energyPortalUserDto.webUserAccountId().toString(),
            EnergyPortalUserDto::displayName
        ));

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
    controllerHelperService.clearSearchFilters(session);
    return ReverseRouter.redirect(on(this.getClass()).getSearchResults(null, null));
  }

  @PostMapping(params = "Filter results")
  ModelAndView filterSearchResults(@ModelAttribute("form") BulkCaseActionSearchFiltersForm form, HttpSession session) {
    controllerHelperService.updateSearchFilters(session, form);
    return ReverseRouter.redirect(on(this.getClass()).getSearchResults(null, null));
  }

  @PostMapping(params = ASSIGN_CASE_OFFICER)
  ModelAndView submitAssignCaseOfficerSelection(
      @Valid @ModelAttribute("form") BulkCaseActionSelectedApplicationsForm form,
      BindingResult bindingResult,
      HttpSession session,
      ServiceUserDetail user
  ) {
    if (bindingResult.hasErrors()) {
      var filtersForm = controllerHelperService.getSearchFiltersForm(session);
      var searchConditions = searchFilterService.getConditions(filtersForm, user);
      var applicationDataItems = bulkCaseActionService.getApplicationDataItems(user, searchConditions);

      var modelAndView = searchResultsModelAndView(applicationDataItems, form);
      addSearchFiltersToModelAndView(modelAndView, filtersForm);

      return modelAndView;
    }

    controllerHelperService.updateSelectedApplicationsForm(session, form);

    return ReverseRouter.redirect(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null));
  }

}
