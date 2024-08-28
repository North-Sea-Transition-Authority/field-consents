package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsController.BULK_ISSUE_CONSENTS;

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
@RequestMapping("bulk-case-actions/bulk-issue-consents/search")
@HasPermission(permissions = RolePermission.AUTHORISE_FCS_CONSENTS)
public class BulkIssueConsentsSearchController {

  private final BulkCaseActionService bulkCaseActionService;
  private final BulkIssueConsentsSearchFilterService searchFilterService;
  private final BulkIssueConsentsService bulkIssueConsentsService;

  BulkIssueConsentsSearchController(
      BulkCaseActionService bulkCaseActionService,
      BulkIssueConsentsSearchFilterService searchFilterService,
      BulkIssueConsentsService bulkIssueConsentsService
  ) {
    this.bulkCaseActionService = bulkCaseActionService;
    this.searchFilterService = searchFilterService;
    this.bulkIssueConsentsService = bulkIssueConsentsService;
  }

  @GetMapping
  public ModelAndView getSearchResults(HttpSession session, ServiceUserDetail user) {
    var sessionContext = BulkIssueConsentsSessionContext.fromSession(session);

    var filtersForm = sessionContext.getSearchFiltersForm();
    var searchConditions = searchFilterService.getConditions(filtersForm);
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
        .addObject("pageTitle", BULK_ISSUE_CONSENTS)
        .addObject("action", BULK_ISSUE_CONSENTS)
        .addObject("applicationDataItemViews", applicationDataItemViews)
        .addObject("form", form)
        .addObject("consentsPendingIssue", bulkIssueConsentsService.getCountOfConsentsNotYetIssued());
  }

  private void addSearchFiltersToModelAndView(ModelAndView modelAndView, BulkIssueConsentsSearchFiltersForm filtersForm) {
    var prefilledOperator = searchFilterService.getPrefilledOrganisation(filtersForm.operatorId());
    var prefilledField = searchFilterService.getPrefilledAsset(filtersForm.fieldAssetKey());
    var prefilledTerminal = searchFilterService.getPrefilledAsset(filtersForm.terminalAssetKey());

    var clearFiltersUrl = ReverseRouter.route(on(this.getClass()).clearSearchFilters(null));
    var fieldAssetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null));
    var terminalAssetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null));
    var searchOperatorRestUrl = ReverseRouter.route(on(OrganisationUnitRestController.class)
        .getOrganisationUnitsForViewer(null, null));

    modelAndView
        .addObject("filtersForm", filtersForm)
        .addObject("prefilledOperator", prefilledOperator)
        .addObject("prefilledField", prefilledField)
        .addObject("prefilledTerminal", prefilledTerminal)
        .addObject("clearFiltersUrl", clearFiltersUrl)
        .addObject("fieldAssetSearchRestUrl", fieldAssetSearchRestUrl)
        .addObject("terminalAssetSearchRestUrl", terminalAssetSearchRestUrl)
        .addObject("operatorSearchRestUrl", searchOperatorRestUrl)
        .addObject("geographicAreaCheckboxes", GeographicArea.getDisplayableOptions())
        .addObject("aceCheckboxes", AceFlagStatus.getDisplayableOptions())
        .addObject("assetTypesWithShoreCheckboxes", AssetTypeWithShore.getDisplayableOptions());
  }

  @GetMapping("clear-filters")
  ModelAndView clearSearchFilters(HttpSession session) {
    BulkIssueConsentsSessionContext.fromSession(session).clearFilters();
    return ReverseRouter.redirect(on(this.getClass()).getSearchResults(null, null));
  }

  @PostMapping(params = "Filter results")
  ModelAndView filterSearchResults(@ModelAttribute("form") BulkIssueConsentsSearchFiltersForm form, HttpSession session) {
    BulkIssueConsentsSessionContext.fromSession(session).setFilters(form);
    return ReverseRouter.redirect(on(this.getClass()).getSearchResults(null, null));
  }

  @PostMapping(params = BULK_ISSUE_CONSENTS)
  ModelAndView submitBulkIssueConsentsSelection(
      @Valid @ModelAttribute("form") BulkCaseActionSelectedApplicationsForm form,
      BindingResult bindingResult,
      HttpSession session,
      ServiceUserDetail user
  ) {
    ModelAndView modelAndView;

    if (bindingResult.hasErrors()) {
      modelAndView = getSearchResults(session, user).addObject("form", form);
    } else {
      modelAndView = ReverseRouter.redirect(on(BulkIssueConsentsController.class).viewSelectedApplications(null, null));
    }

    BulkIssueConsentsSessionContext.fromSession(session).setSelectedApplicationsForm(form);
    return modelAndView;
  }
}
