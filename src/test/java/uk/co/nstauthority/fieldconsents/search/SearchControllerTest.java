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
import static uk.co.nstauthority.fieldconsents.search.SearchController.SEARCH_TITLE;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = SearchController.class)
class SearchControllerTest extends AbstractControllerTest {

  public static final String SEARCH_VIEW_NAME = "fcs/search/search";

  @MockBean
  private SearchService searchService;
  @MockBean
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  private SearchFilterForm form;

  private List<SearchResultItem> searchResultItems;

  private RestSearchItem orgUnitRestSearchItem;

  private RestSearchItem assetFieldRestSearchItem;

  @BeforeEach
  void setUp() {
    form = new SearchFilterForm();
    searchResultItems = List.of(ApplicationDataItemUtil.getSearchResultItem());
    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
    assetFieldRestSearchItem = ApplicationDataFilterFormTestUtil.FIELD_REST_SEARCH_ITEM;
    when(applicationDataFilterFormService.getPrefilledAsset(any())).thenReturn(assetFieldRestSearchItem);
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
  }

  @SecurityTest
  void getSearch_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSearch_whenRegulatorUser() throws Exception {
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class)
                .getSearch(form)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchApplications_whenIndustryUser() throws Exception {
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(SearchController.class)
                .searchApplications(form, user)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchApplications_whenNeitherRegulatorNorIndustryUser() throws Exception {
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(false);

    mockMvc.perform(
            post(ReverseRouter.route(on(SearchController.class)
                .searchApplications(form, user)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isOk());
  }

  @Test
  void getSearch_IndustryUser() throws Exception {
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertSearchModel(model);
  }

  @Test
  void getSearch_RegulatorUser() throws Exception {
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertSearchModel(model);
  }

  private void assertSearchModel(Map<String, Object> model) {
    assertThat(model)
        .containsEntry("clearFiltersUrl", ReverseRouter.route(on(SearchController.class).clearSearchFilter(null)))
        .containsEntry("appStatuses", ApplicationVersionStatus.getSearchOptions())
        .containsEntry("appTypes", ApplicationType.getDisplayableOptions())
        .containsEntry("durationTypes", ConsentLengthType.getConsentLengthOptions())
        .containsEntry("prefilledOperator", orgUnitRestSearchItem)
        .containsEntry("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .containsEntry("prefilledField", assetFieldRestSearchItem)
        .containsEntry("fieldAssetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchFieldAssets(null)))
        .containsEntry("pageTitle", SEARCH_TITLE);
  }

  @Test
  void searchApplications_RegulatorUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
    when(searchService.getRegulatorSearchResultItems(any(SearchFilterForm.class), any(ServiceUserDetail.class)))
        .thenReturn(searchResultItems);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SearchController.class).searchApplications(null, user)))
        .with(csrf())
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute("showResults", true))
        .andExpect(model().attribute("searchResultItems", searchResultItems))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertSearchModel(model);
  }

  @Test
  void searchApplications_IndustryUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
    when(searchService.getIndustrySearchResultItems(any(SearchFilterForm.class), any(ServiceUserDetail.class)))
        .thenReturn(searchResultItems);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SearchController.class).searchApplications(null, user)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute("showResults", true))
        .andExpect(model().attribute("searchResultItems", searchResultItems))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertSearchModel(model);
  }

  @Test
  void searchApplications_neitherRegulatorNorIndustryUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(false);
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(SearchController.class).searchApplications(null, user)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(SEARCH_VIEW_NAME))
        .andExpect(model().attribute("showResults", true))
        .andExpect(model().attribute("searchResultItems", Collections.emptyList()))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertSearchModel(model);
  }

  @Test
  void clearSearchFilter() throws Exception {
    var expectedRedirectUrl = ReverseRouter.route(on(SearchController.class).getSearch(null));

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class).clearSearchFilter(form)))
                .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirectUrl));
  }
}
