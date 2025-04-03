package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSelectedApplicationsForm;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ContextConfiguration(classes = BulkAssignCaseOfficerSearchController.class)
class BulkAssignCaseOfficerSearchControllerTest extends AbstractControllerTest {

  private static final String SESSION_ATTRIBUTE = "bulkCaseActions-assignCaseOfficer";
  private static final String ASSIGN_CASE_OFFICER = "Assign case officer";
  private static final String FILTER_RESULTS = "Filter results";

  private static final Map<String, String> CASE_OFFICER_DISPLAY_OPTIONS = Map.of("123", "case officer 1");

  @MockitoBean
  private BulkCaseActionService bulkCaseActionService;

  @MockitoBean
  private BulkAssignCaseOfficerSearchFilterService searchFilterService;

  private MockHttpSession httpSession;

  private BulkAssignCaseOfficerSearchFiltersForm bulkAssignCaseOfficerSearchFiltersForm;

  private BulkCaseActionSelectedApplicationsForm bulkCaseActionSelectedApplicationsForm;

  private BulkAssignCaseOfficerSessionContext bulkAssignCaseOfficerSessionContext;

  @BeforeEach
  void setUp() {
    this.httpSession = new MockHttpSession();
    this.bulkAssignCaseOfficerSearchFiltersForm = BulkAssignCaseOfficerSearchFiltersForm.empty();
    this.bulkCaseActionSelectedApplicationsForm = new BulkCaseActionSelectedApplicationsForm();
    this.bulkAssignCaseOfficerSessionContext = new BulkAssignCaseOfficerSessionContext(
        httpSession,
        bulkAssignCaseOfficerSearchFiltersForm,
        bulkCaseActionSelectedApplicationsForm
    );
    this.httpSession.setAttribute(SESSION_ATTRIBUTE, this.bulkAssignCaseOfficerSessionContext);
  }

  @SecurityTest
  void getSearchResults() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    this.bulkCaseActionSelectedApplicationsForm.setSelectedApplicationIds(Set.of(1, 2, 3));
    assertThat(this.bulkCaseActionSelectedApplicationsForm.getSelectedApplicationIds()).containsOnly(1, 2, 3);

    var applicationDataItemView1 = ApplicationDataItemView.newBuilder()
        .withApplicationId(1)
        .withReference("PCON/8001/0 (Version 1)")
        .withType(ApplicationType.PRODUCTION.getDisplayName())
        .withDuration(ConsentLengthType.LONG_TERM.getDisplayName())
        .withAceFlag(true)
        .withAsset("asset")
        .withGeographicArea("geographic area")
        .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
        .build();

    var applicationDataItemView2 = ApplicationDataItemView.newBuilder()
        .withApplicationId(2)
        .withReference("VCON/8002/0 (Version 1)")
        .withType(ApplicationType.VENT.getDisplayName())
        .withDuration(ConsentLengthType.SHORT_TERM.getDisplayName())
        .withAceFlag(true)
        .withAsset("asset")
        .withGeographicArea("geographic area")
        .withStatus(ApplicationVersionStatus.SUBMITTED.getDisplayName())
        .build();

    var applicationDataItemViews = List.of(applicationDataItemView1, applicationDataItemView2);

    var jooqConditions = List.<Condition>of();
    when(searchFilterService.getConditions(bulkAssignCaseOfficerSessionContext.getSearchFiltersForm(), user)).thenReturn(jooqConditions);
    when(bulkCaseActionService.getApplicationDataItemViews(user, jooqConditions)).thenReturn(applicationDataItemViews);

    mockAddSearchFiltersToModelAndView();

    mockMvc.perform(get(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null)))
            .session(httpSession)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"))
        .andExpect(model().attribute("pageTitle", ASSIGN_CASE_OFFICER))
        .andExpect(model().attribute("action", ASSIGN_CASE_OFFICER))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews))
        .andExpect(model().attribute("form", bulkAssignCaseOfficerSessionContext.getSelectedApplicationsForm()))
        .andExpectAll(addSearchFiltersToModelAndViewModelAssertions());

    // 3 should be removed because an application data item view was not returned for it, so it's no longer selectable
    assertThat(this.bulkCaseActionSelectedApplicationsForm.getSelectedApplicationIds()).containsOnly(1, 2);
  }

  private void mockAddSearchFiltersToModelAndView() {
    when(searchFilterService.getPrefilledOrganisation(null)).thenReturn(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
    when(searchFilterService.getPrefilledAsset(null)).thenReturn(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
    when(searchFilterService.getCaseOfficerDisplayOptions()).thenReturn(CASE_OFFICER_DISPLAY_OPTIONS);
  }

  private ResultMatcher[] addSearchFiltersToModelAndViewModelAssertions() {
    return new ResultMatcher[]{
        model().attribute("filtersForm", bulkAssignCaseOfficerSessionContext.getSearchFiltersForm()),

        model().attribute("prefilledOperator", RestSearchItem.EMPTY_REST_SEARCH_ITEM),
        model().attribute("prefilledField", RestSearchItem.EMPTY_REST_SEARCH_ITEM),
        model().attribute("prefilledTerminal", RestSearchItem.EMPTY_REST_SEARCH_ITEM),

        model().attribute("clearFiltersUrl", ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).clearSearchFilters(null))),
        model().attribute("fieldAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null))),
        model().attribute("terminalAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null))),
        model().attribute("operatorSearchRestUrl", ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null))),

        model().attribute("geographicAreaCheckboxes", GeographicArea.getDisplayableOptions()),
        model().attribute("aceCheckboxes", AceFlagStatus.getDisplayableOptions()),
        model().attribute("assetTypesWithShoreCheckboxes", AssetTypeWithShore.getDisplayableOptions()),
        model().attribute("caseOfficerOptions", CASE_OFFICER_DISPLAY_OPTIONS),
    };
  }

  @SecurityTest
  void clearFilters() throws Exception {
    this.bulkAssignCaseOfficerSearchFiltersForm = new BulkAssignCaseOfficerSearchFiltersForm(
        123,
        "fieldAssetKey",
        "terminalAssetKey",
        "caseOfficerWuaId",
        Set.of(),
        Set.of(),
        Set.of()
    );

    this.bulkAssignCaseOfficerSessionContext.setFilters(this.bulkAssignCaseOfficerSearchFiltersForm);
    assertThat(bulkAssignCaseOfficerSessionContext.getSearchFiltersForm()).isEqualTo(this.bulkAssignCaseOfficerSearchFiltersForm);

    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).clearSearchFilters(null)))
            .with(user(user))
            .session(httpSession))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))));

    assertThat(bulkAssignCaseOfficerSessionContext.getSearchFiltersForm()).isEqualTo(BulkAssignCaseOfficerSearchFiltersForm.empty());
  }

  @SecurityTest
  void filterSearchResults() throws Exception {
    assertThat(this.bulkAssignCaseOfficerSessionContext.getSearchFiltersForm().operatorId()).isNull();

    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    var operatorId = 123;

    mockMvc.perform(post(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).filterSearchResults(null, null)))
            .with(user(user))
            .with(csrf())
            .param("operatorId", String.valueOf(operatorId))
            .param(FILTER_RESULTS, FILTER_RESULTS)
            .session(httpSession))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))));

    assertThat(this.bulkAssignCaseOfficerSessionContext.getSearchFiltersForm().operatorId()).isEqualTo(operatorId);
  }

  @SecurityTest
  void submitAssignCaseOfficerSelection() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    assertThat(this.bulkAssignCaseOfficerSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).isEmpty();

    mockMvc.perform(post(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).submitAssignCaseOfficerSelection(null, null, null, null)))
            .with(user(user))
            .session(httpSession)
            .with(csrf())
            .param("selectedApplicationIds","1, 2, 3")
            .param(ASSIGN_CASE_OFFICER, ASSIGN_CASE_OFFICER))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null))));

    assertThat(this.bulkAssignCaseOfficerSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).containsOnly(1, 2, 3);
  }

  @SecurityTest
  void submitAssignCaseOfficerSelection_withValidationError() throws Exception {
    this.bulkCaseActionSelectedApplicationsForm.setSelectedApplicationIds(Set.of(1, 2, 3));
    assertThat(this.bulkAssignCaseOfficerSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).containsOnly(1, 2, 3);

    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CASE_MANAGER)).thenReturn(true);

    mockAddSearchFiltersToModelAndView();

    mockMvc.perform(post(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).submitAssignCaseOfficerSelection(null, null, null, null)))
            .with(user(user))
            .session(httpSession)
            .with(csrf())
            .param(ASSIGN_CASE_OFFICER, ASSIGN_CASE_OFFICER))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"));

    assertThat(this.bulkAssignCaseOfficerSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).isEmpty();
  }

}
