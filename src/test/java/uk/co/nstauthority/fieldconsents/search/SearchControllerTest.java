package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.search.SearchController.SEARCH_RESULT_ITEMS;
import static uk.co.nstauthority.fieldconsents.search.SearchController.SEARCH_TITLE;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;

@ContextConfiguration(classes = SearchController.class)
class SearchControllerTest extends AbstractControllerTest {

  private static final String SEARCH_VIEW_NAME = "fcs/search/search";

  private static final String EXPECTED_REDIRECT_URL = ReverseRouter.route(on(SearchController.class).getSearch(null, null));

  @MockBean
  private SearchService searchService;

  @MockBean
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  private SearchFilterForm form;

  private SearchSession searchSession;

  private List<ApplicationDataItemView> applicationDataItemViews;

  private RestSearchItem orgUnitRestSearchItem;

  private RestSearchItem assetFieldRestSearchItem;

  private RestSearchItem assetTerminalRestSearchItem;

  @BeforeEach
  void setUp() {
    form = new SearchFilterForm();
    form.setFieldAssetKey(FIELD1_ASSET_KEY);
    form.setTerminalAssetKey(TERMINAL1_ASSET_KEY);
    searchSession = new SearchSession(form);
    applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(FIELD1_ASSET_KEY)).thenReturn(assetFieldRestSearchItem);
    assetTerminalRestSearchItem = ApplicationDataFilterFormTestUtil.TERMINAL1_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(TERMINAL1_ASSET_KEY)).thenReturn(assetTerminalRestSearchItem);
  }

  @SecurityTest
  void getSearch_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSearch_whenUserHasPermissions() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    searchSession.update(form);

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class)
                .getSearch(searchSession, user)))
                .with(user(user))
                .flashAttr("form", form)
                .flashAttr("searchSession", searchSession)
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchApplications_whenUserHasPermissions() throws Exception {
    mockMvc.perform(
            post(ReverseRouter.route(on(SearchController.class)
                .searchApplications(form, searchSession)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));
  }

  @Test
  void getSearch_IndustryUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(searchService.getIndustryApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class)))
        .thenReturn(applicationDataItemViews);
    searchSession.update(form);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
        .with(user(user))
        .flashAttr("form", form)
        .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry(SEARCH_RESULT_ITEMS, applicationDataItemViews);
    assertSearchModel(model);
  }

  @Test
  void getSearch_IndustryUser_10Results() throws Exception {
    var applicationDataItemViews = IntStream.range(0, 10)
        .mapToObj(i -> ApplicationDataItemUtil.getApplicationDataItemView())
        .toList();

    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(searchService.getIndustryApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class))).thenReturn(applicationDataItemViews);
    searchSession.update(form);

    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute(SEARCH_RESULT_ITEMS, applicationDataItemViews))
        .andExpect(model().attribute("searchResultsLimited", false));
  }

  @Test
  void getSearch_IndustryUser_over300Results() throws Exception {
    var applicationDataItemViews = IntStream.range(0, 350)
        .mapToObj(i -> ApplicationDataItemUtil.getApplicationDataItemView())
        .toList();

    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(searchService.getIndustryApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class))).thenReturn(applicationDataItemViews);
    searchSession.update(form);

    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute(SEARCH_RESULT_ITEMS, applicationDataItemViews.subList(0, 300)))
        .andExpect(model().attribute("searchResultsLimited", true));
  }

  @Test
  void getSearch_RegulatorUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(searchService.getRegulatorApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class)))
        .thenReturn(applicationDataItemViews);
    searchSession.update(form);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
        .with(user(user))
        .flashAttr("form", form)
        .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry(SEARCH_RESULT_ITEMS, applicationDataItemViews)
        .containsEntry("aceStatuses", AceFlagStatus.getDisplayableOptions());
    assertSearchModel(model);
  }

  @Test
  void getSearch_RegulatorUser_10Results() throws Exception {
    var applicationDataItemViews = IntStream.range(0, 10)
        .mapToObj(i -> ApplicationDataItemUtil.getApplicationDataItemView())
        .toList();

    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(searchService.getRegulatorApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class))).thenReturn(applicationDataItemViews);
    searchSession.update(form);

    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute(SEARCH_RESULT_ITEMS, applicationDataItemViews))
        .andExpect(model().attribute("searchResultsLimited", false));
  }

  @Test
  void getSearch_RegulatorUser_over300Results() throws Exception {
    var applicationDataItemViews = IntStream.range(0, 350)
        .mapToObj(i -> ApplicationDataItemUtil.getApplicationDataItemView())
        .toList();

    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(searchService.getRegulatorApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class))).thenReturn(applicationDataItemViews);
    searchSession.update(form);

    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute(SEARCH_RESULT_ITEMS, applicationDataItemViews.subList(0, 300)))
        .andExpect(model().attribute("searchResultsLimited", true));
  }

  @Test
  void getSearch_ConsulteeUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(false);
    when(teamService.isConsulteeUser(user)).thenReturn(true);
    when(searchService.getConsulteeApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class)))
        .thenReturn(applicationDataItemViews);
    searchSession.update(form);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry(SEARCH_RESULT_ITEMS, applicationDataItemViews)
        .containsEntry("aceStatuses", AceFlagStatus.getDisplayableOptions());
    assertSearchModel(model);
  }

  @Test
  void getSearch_ConsulteeUser_10Results() throws Exception {
    var applicationDataItemViews = IntStream.range(0, 10)
        .mapToObj(i -> ApplicationDataItemUtil.getApplicationDataItemView())
        .toList();

    when(teamService.isConsulteeUser(user)).thenReturn(true);
    when(searchService.getConsulteeApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class))).thenReturn(applicationDataItemViews);
    searchSession.update(form);

    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute(SEARCH_RESULT_ITEMS, applicationDataItemViews))
        .andExpect(model().attribute("searchResultsLimited", false));
  }

  @Test
  void getSearch_ConsulteeUser_over300Results() throws Exception {
    var applicationDataItemViews = IntStream.range(0, 350)
        .mapToObj(i -> ApplicationDataItemUtil.getApplicationDataItemView())
        .toList();

    when(teamService.isConsulteeUser(user)).thenReturn(true);
    when(searchService.getConsulteeApplicationDataItemViews(any(SearchFilterForm.class), any(ServiceUserDetail.class))).thenReturn(applicationDataItemViews);
    searchSession.update(form);

    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute(SEARCH_RESULT_ITEMS, applicationDataItemViews.subList(0, 300)))
        .andExpect(model().attribute("searchResultsLimited", true));
  }

  @Test
  void getSearch_userNotRecognised() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(false);
    when(teamService.isConsulteeUser(user)).thenReturn(false);
    applicationDataItemViews = Collections.emptyList();
    searchSession.update(form);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry(SEARCH_RESULT_ITEMS, applicationDataItemViews);
    assertSearchModel(model);
  }

  @Test
  void getSearch_withSearchNotInvoked() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    assetFieldRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    assetTerminalRestSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(null)).thenReturn(assetFieldRestSearchItem);
    searchSession.clearSession();

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(searchSession, null)))
            .with(user(user))
            .flashAttr("form", form)
            .flashAttr("searchSession", searchSession))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();
    assertThat(model)
        .doesNotContainEntry(SEARCH_RESULT_ITEMS, applicationDataItemViews);
    assertSearchModel(model);
  }

  private void assertSearchModel(Map<String, Object> model) {
    assertThat(model)
        .containsEntry("clearFiltersUrl", ReverseRouter.route(on(SearchController.class).clearSearchFilter(null, null)))
        .containsEntry("appStatuses", ApplicationVersionStatus.getSearchOptions())
        .containsEntry("appTypes", ApplicationType.getDisplayableOptions())
        .containsEntry("durationTypes", ConsentLengthType.getConsentLengthOptions())
        .containsEntry("prefilledOperator", orgUnitRestSearchItem)
        .containsEntry("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .containsEntry("prefilledField", assetFieldRestSearchItem)
        .containsEntry("fieldAssetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchFieldAssetsForUser(null, null)))
        .containsEntry("prefilledTerminal", assetTerminalRestSearchItem)
        .containsEntry("terminalAssetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchTerminalAssetsForUser(null, null)))
        .containsEntry("assetTypesWithShore", AssetTypeWithShore.getDisplayableOptions())
        .containsEntry("pageTitle", SEARCH_TITLE);
  }

  @Test
  void searchApplications() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(SearchController.class)
            .searchApplications(null, null)))
            .with(csrf())
            .with(user(user)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));
  }

  @Test
  void clearSearchFilter() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class).clearSearchFilter(null, null)))
                .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));
  }
}
