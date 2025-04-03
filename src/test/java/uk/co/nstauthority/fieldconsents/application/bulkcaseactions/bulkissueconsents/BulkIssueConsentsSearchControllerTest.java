package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

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

@ContextConfiguration(classes = BulkIssueConsentsSearchController.class)
class BulkIssueConsentsSearchControllerTest extends AbstractControllerTest {

  private static final String SESSION_ATTRIBUTE = "bulkCaseActions-issueConsents";
  private static final String BULK_ISSUE_CONSENTS = "Bulk issue consents";
  private static final String FILTER_RESULTS = "Filter results";

  @MockitoBean
  private BulkCaseActionService bulkCaseActionService;

  @MockitoBean
  private BulkIssueConsentsSearchFilterService searchFilterService;

  @MockitoBean
  private BulkIssueConsentsService bulkIssueConsentsTaskService;

  private MockHttpSession httpSession;

  private BulkIssueConsentsSearchFiltersForm bulkIssueConsentsSearchFiltersForm;

  private BulkCaseActionSelectedApplicationsForm bulkCaseActionSelectedApplicationsForm;

  private BulkIssueConsentsSessionContext bulkIssueConsentsSessionContext;

  @BeforeEach
  void setUp() {
    this.httpSession = new MockHttpSession();
    this.bulkIssueConsentsSearchFiltersForm = BulkIssueConsentsSearchFiltersForm.empty();
    this.bulkCaseActionSelectedApplicationsForm = new BulkCaseActionSelectedApplicationsForm();
    this.bulkIssueConsentsSessionContext = new BulkIssueConsentsSessionContext(
        httpSession,
        bulkIssueConsentsSearchFiltersForm,
        bulkCaseActionSelectedApplicationsForm
    );
    this.httpSession.setAttribute(SESSION_ATTRIBUTE, this.bulkIssueConsentsSessionContext);
  }

  @SecurityTest
  void getSearchResults() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    this.bulkCaseActionSelectedApplicationsForm.setSelectedApplicationIds(Set.of(1, 2, 3));
    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).containsOnly(1, 2, 3);

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
    when(searchFilterService.getConditions(bulkIssueConsentsSessionContext.getSearchFiltersForm())).thenReturn(jooqConditions);
    when(bulkCaseActionService.getApplicationDataItemViews(user, jooqConditions)).thenReturn(applicationDataItemViews);

    var consentsPendingIssue = 12L;
    when(bulkIssueConsentsTaskService.getCountOfConsentsNotYetIssued()).thenReturn(consentsPendingIssue);

    mockAddSearchFiltersToModelAndView();

    mockMvc.perform(get(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null)))
        .session(httpSession)
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"))
        .andExpect(model().attribute("pageTitle", BULK_ISSUE_CONSENTS))
        .andExpect(model().attribute("action", BULK_ISSUE_CONSENTS))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews))
        .andExpect(model().attribute("form", bulkIssueConsentsSessionContext.getSelectedApplicationsForm()))
        .andExpect(model().attribute("consentsPendingIssue", consentsPendingIssue))
        .andExpectAll(addSearchFiltersToModelAndViewModelAssertions());

    // 3 should be removed because an application data item view was not returned for it, so it's no longer selectable
    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).containsOnly(1, 2);
  }

  private void mockAddSearchFiltersToModelAndView() {
    when(searchFilterService.getPrefilledOrganisation(null)).thenReturn(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
    when(searchFilterService.getPrefilledAsset(null)).thenReturn(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  private ResultMatcher[] addSearchFiltersToModelAndViewModelAssertions() {
    return new ResultMatcher[]{
        model().attribute("filtersForm", bulkIssueConsentsSessionContext.getSearchFiltersForm()),

        model().attribute("prefilledOperator", RestSearchItem.EMPTY_REST_SEARCH_ITEM),
        model().attribute("prefilledField", RestSearchItem.EMPTY_REST_SEARCH_ITEM),
        model().attribute("prefilledTerminal", RestSearchItem.EMPTY_REST_SEARCH_ITEM),

        model().attribute("clearFiltersUrl", ReverseRouter.route(on(BulkIssueConsentsSearchController.class).clearSearchFilters(null))),
        model().attribute("fieldAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null))),
        model().attribute("terminalAssetSearchRestUrl", ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null))),
        model().attribute("operatorSearchRestUrl", ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null))),

        model().attribute("geographicAreaCheckboxes", GeographicArea.getDisplayableOptions()),
        model().attribute("aceCheckboxes", AceFlagStatus.getDisplayableOptions()),
        model().attribute("assetTypesWithShoreCheckboxes", AssetTypeWithShore.getDisplayableOptions()),
    };
  }

  @SecurityTest
  void clearFilters() throws Exception {
    this.bulkIssueConsentsSearchFiltersForm = new BulkIssueConsentsSearchFiltersForm(
        123,
        "fieldAssetKey",
        "terminalAssetKey",
        Set.of(),
        Set.of(),
        Set.of()
    );

    this.bulkIssueConsentsSessionContext.setFilters(this.bulkIssueConsentsSearchFiltersForm);
    assertThat(bulkIssueConsentsSessionContext.getSearchFiltersForm()).isEqualTo(this.bulkIssueConsentsSearchFiltersForm);

    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).clearSearchFilters(null)))
        .with(user(user))
        .session(httpSession))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))));

    assertThat(bulkIssueConsentsSessionContext.getSearchFiltersForm()).isEqualTo(BulkIssueConsentsSearchFiltersForm.empty());
  }

  @SecurityTest
  void filterSearchResults() throws Exception {
    assertThat(this.bulkIssueConsentsSessionContext.getSearchFiltersForm().operatorId()).isNull();

    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    var operatorId = 123;

    mockMvc.perform(post(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).filterSearchResults(null, null)))
            .with(user(user))
            .with(csrf())
            .param("operatorId", String.valueOf(operatorId))
            .param(FILTER_RESULTS, FILTER_RESULTS)
            .session(httpSession))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))));

    assertThat(this.bulkIssueConsentsSessionContext.getSearchFiltersForm().operatorId()).isEqualTo(operatorId);
  }

  @SecurityTest
  void submitBulkIssueConsentsSelection() throws Exception {
    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).isEmpty();

    mockMvc.perform(post(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).submitBulkIssueConsentsSelection(null, null, null, null)))
        .with(user(user))
        .session(httpSession)
        .with(csrf())
        .param("selectedApplicationIds","1, 2, 3")
        .param(BULK_ISSUE_CONSENTS, BULK_ISSUE_CONSENTS))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkIssueConsentsController.class).viewSelectedApplications(null, null))));

    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).containsOnly(1, 2, 3);
  }

  @SecurityTest
  void submitBulkIssueConsentsSelection_withValidationError() throws Exception {
    this.bulkCaseActionSelectedApplicationsForm.setSelectedApplicationIds(Set.of(1, 2, 3));
    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).containsOnly(1, 2, 3);

    when(teamQueryService.userHasStaticRole(user, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    mockAddSearchFiltersToModelAndView();

    mockMvc.perform(post(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).submitBulkIssueConsentsSelection(null, null, null, null)))
            .with(user(user))
            .session(httpSession)
            .with(csrf())
            .param(BULK_ISSUE_CONSENTS, BULK_ISSUE_CONSENTS))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"));

    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).isEmpty();
  }

}