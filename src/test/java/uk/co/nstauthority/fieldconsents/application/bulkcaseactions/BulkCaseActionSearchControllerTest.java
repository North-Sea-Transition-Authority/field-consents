package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = BulkCaseActionSearchController.class)
class BulkCaseActionSearchControllerTest extends AbstractControllerTest {

  private static final Class<BulkCaseActionSearchController> CONTROLLER_CLASS = BulkCaseActionSearchController.class;
  private static final String VIEW_NAME = "fcs/application/bulk-case-actions/search";

  @MockBean
  private BulkCaseActionService bulkCaseActionService;

  @MockBean
  private BulkCaseActionControllerHelperService controllerHelperService;

  @MockBean
  private BulkCaseActionSearchFilterService searchFilterService;

  private MockHttpSession session;

  private RestSearchItem prefilledOperator;

  private RestSearchItem prefilledField;

  private RestSearchItem prefilledTerminal;

  private Map<String, String> caseOfficerOptions;

  private List<String> bulkActions;

  @BeforeEach
  void setUp() {
    session = new MockHttpSession();

    prefilledOperator = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    prefilledField = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    prefilledTerminal = RestSearchItem.EMPTY_REST_SEARCH_ITEM;

    var caseOfficer = new EnergyPortalUserDto(
        1L,
        1L,
        "title",
        "forename",
        "surname",
        "emailAddress",
        "telephoneNumber",
        false,
        true
    );

    caseOfficerOptions = Map.of(
        caseOfficer.webUserAccountId().toString(),
        caseOfficer.displayName()
    );

    bulkActions = List.of(
        BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER
    );
  }

  @Test
  void pageTitle() {
    assertThat(BulkCaseActionSearchController.PAGE_TITLE).isEqualTo("Bulk case actions");
  }

  @SecurityTest
  void getSearchResults_redirectedToLoginUrlWhenUnauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))).andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSearchResults_userDoesNotHavePermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .getSearchResults(null, null)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getSearchResults_nothingSelected() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var filtersForm = BulkCaseActionSearchFiltersForm.empty();
    when(controllerHelperService.getSearchFiltersForm(session)).thenReturn(filtersForm);

    var jooqConditions = List.<Condition>of();
    when(searchFilterService.getConditions(filtersForm, user)).thenReturn(jooqConditions);

    var applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
    when(bulkCaseActionService.getApplicationDataItemViews(user, jooqConditions)).thenReturn(applicationDataItemViews);

    var searchForm = BulkCaseActionSelectedApplicationsForm.empty();
    when(controllerHelperService.getSelectedApplicationsForm(session, applicationDataItemViews)).thenReturn(searchForm);

    when(bulkCaseActionService.getBulkActions()).thenReturn(bulkActions);

    mockServiceCallsForSearchFilters(filtersForm);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
        .session(session)
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"))
        .andExpect(model().attribute("pageTitle", BulkCaseActionSearchController.PAGE_TITLE))
        .andExpect(model().attribute("actions", List.of(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews))
        .andExpect(model().attribute("form", searchForm));
  }

  @Test
  void getSearchResults_withPreviouslySelectedApplications() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var applicationDataItem1 = applicationDataItemBuilderWithDefaults(1).build();
    var applicationDataItem2 = applicationDataItemBuilderWithDefaults(2).build();
    var applicationDataItem3 = applicationDataItemBuilderWithDefaults(3).build();

    var filtersForm = BulkCaseActionSearchFiltersForm.empty();
    when(controllerHelperService.getSearchFiltersForm(session)).thenReturn(filtersForm);

    var jooqConditions = List.<Condition>of();
    when(searchFilterService.getConditions(filtersForm, user)).thenReturn(jooqConditions);

    var applicationDataItemViews = List.of(applicationDataItem1, applicationDataItem2, applicationDataItem3);
    when(bulkCaseActionService.getApplicationDataItemViews(user, jooqConditions)).thenReturn(applicationDataItemViews);

    var searchForm = new BulkCaseActionSelectedApplicationsForm(Set.of("1", "2", "3"));
    when(controllerHelperService.getSelectedApplicationsForm(session, applicationDataItemViews)).thenReturn(searchForm);

    when(bulkCaseActionService.getBulkActions()).thenReturn(bulkActions);

    mockServiceCallsForSearchFilters(filtersForm);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
        .session(session)
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", BulkCaseActionSearchController.PAGE_TITLE))
        .andExpect(model().attribute("actions", List.of(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews))
        .andExpect(model().attribute("form", searchForm))
        .andExpectAll(containsSearchFilterData(filtersForm));
  }

  @Test
  void submitAssignCaseOfficerSelection() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var selectedApplicationIds = Set.of("1", "2", "3");

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).submitAssignCaseOfficerSelection(null, null, null, null)))
        .session(session)
        .with(user(user))
        .with(csrf())
        .param(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER, BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)
        .param("selectedApplicationIds", String.join(", ", selectedApplicationIds)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null))));

    verify(controllerHelperService).updateSelectedApplicationsForm(session, new BulkCaseActionSelectedApplicationsForm(selectedApplicationIds));
  }

  @Test
  void clearSearchFilters() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).clearSearchFilters(null)))
        .session(session)
        .with(user(user)));
    verify(controllerHelperService).clearSearchFilters(session);
  }

  @Test
  void filterSearchResults_emptyForm() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).filterSearchResults(null, null)))
        .session(session)
        .with(user(user))
        .with(csrf())
        .param("Filter results", "Filter results") // this is the button which submits the filters
    );

    verify(controllerHelperService).updateSearchFilters(session, new BulkCaseActionSearchFiltersForm(
        null, null, null, null, null, null, null
    ));
  }

  @Test
  void filterSearchResults() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).filterSearchResults(null, null)))
        .session(session)
        .with(user(user))
        .with(csrf())
        .param("Filter results", "Filter results") // this is the button which submits the filters
        .param("operatorId", "1")
        .param("fieldAssetKey", "2")
        .param("terminalAssetKey", "3")
        .param("caseOfficerWuaId", "4")
        .param("includeUnassignedCaseOfficer", "true")
        .param("geographicAreas", "CNS")
        .param("geographicAreas", "IS")
        .param("geographicAreas", "LAND")
        .param("aceFlagStatuses", "ACE")
        .param("aceFlagStatuses", "NON_ACE")
        .param("assetTypesWithShore", "TERMINAL")
        .param("assetTypesWithShore", "FIELD_OFFSHORE")
    );

    verify(controllerHelperService).updateSearchFilters(session, new BulkCaseActionSearchFiltersForm(
        1,
        "2",
        "3",
        "4",
        Set.of(GeographicArea.CNS, GeographicArea.IS, GeographicArea.LAND),
        Set.of(AceFlagStatus.ACE, AceFlagStatus.NON_ACE),
        Set.of(AssetTypeWithShore.TERMINAL, AssetTypeWithShore.FIELD_OFFSHORE)
    ));
  }

  @Test
  void submitAssignCaseOfficerSelection_nothingSelected() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var filtersForm = BulkCaseActionSearchFiltersForm.empty();
    when(controllerHelperService.getSearchFiltersForm(session)).thenReturn(filtersForm);

    var jooqConditions = List.<Condition>of();
    when(searchFilterService.getConditions(filtersForm, user)).thenReturn(jooqConditions);

    var applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
    when(bulkCaseActionService.getApplicationDataItemViews(user, jooqConditions)).thenReturn(applicationDataItemViews);

    mockServiceCallsForSearchFilters(filtersForm);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).submitAssignCaseOfficerSelection(null, null, null, null)))
            .session(session)
            .with(user(user))
            .with(csrf())
            .param(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER, BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME))
        .andExpectAll(containsSearchFilterData(filtersForm));

    verify(controllerHelperService, never()).updateSelectedApplicationsForm(any(), any());
  }

  private void mockServiceCallsForSearchFilters(BulkCaseActionSearchFiltersForm filtersForm) {
    when(searchFilterService.getPrefilledOrganisation(filtersForm.operatorId())).thenReturn(prefilledField);
    when(searchFilterService.getPrefilledAsset(filtersForm.fieldAssetKey())).thenReturn(prefilledField);
    when(searchFilterService.getPrefilledAsset(filtersForm.terminalAssetKey())).thenReturn(prefilledTerminal);
    when(searchFilterService.getCaseOfficerDisplayOptions()).thenReturn(caseOfficerOptions);
  }

  private ResultMatcher[] containsSearchFilterData(BulkCaseActionSearchFiltersForm filtersForm) {
    var clearFiltersUrl =  ReverseRouter.route(on(CONTROLLER_CLASS).clearSearchFilters(null));
    var fieldAssetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null));
    var terminalAssetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null));
    var searchOperatorRestUrl = ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null));

    return new ResultMatcher[] {
        model().attribute("filtersForm", filtersForm),
        model().attribute("clearFiltersUrl", clearFiltersUrl),
        model().attribute("prefilledOperator", prefilledOperator),
        model().attribute("prefilledField", prefilledField),
        model().attribute("prefilledTerminal", prefilledTerminal),
        model().attribute("fieldAssetSearchRestUrl", fieldAssetSearchRestUrl),
        model().attribute("terminalAssetSearchRestUrl", terminalAssetSearchRestUrl),
        model().attribute("operatorSearchRestUrl", searchOperatorRestUrl),
        model().attribute("geographicAreaCheckboxes", GeographicArea.getDisplayableOptions()),
        model().attribute("aceCheckboxes", AceFlagStatus.getDisplayableOptions()),
        model().attribute("assetTypesWithShoreCheckboxes", AssetTypeWithShore.getDisplayableOptions()),
        model().attribute("caseOfficerOptions", caseOfficerOptions),

    };
  }

  private ApplicationDataItemView.Builder applicationDataItemBuilderWithDefaults(Integer applicationId) {
    return ApplicationDataItemView.newBuilder()
        .withApplicationId(applicationId)
        .withType("")
        .withReference("")
        .withOperator("")
        .withDuration("")
        .withAceFlag(true)
        .withAsset("")
        .withGeographicArea("")
        .withStatus("")
        .withCaseOfficer("")
        .withTechnicalReviewer("")
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withLicences("");
  }

}
