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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = SearchController.class)
class SearchControllerTest extends AbstractApplicationControllerTest {

  public static final String SEARCH_VIEW_NAME = "fcs/search/search";

  @MockBean
  private SearchService searchService;
  @MockBean
  private SearchFilterFormService searchFilterFormService;

  private ApplicationDataFilterForm form;

  private List<SearchResultItem> searchResultItems;

  private RestSearchItem orgUnitRestSearchItem;

  @BeforeEach
  void setUp() {
    form = new ApplicationDataFilterForm();
    searchResultItems = List.of(ApplicationDataItemUtil.getSearchResultItem());
    orgUnitRestSearchItem = ApplicationDataFilterFormTestUtil.ORGANISATION_REST_SEARCH_ITEM;
    when(searchFilterFormService.getPrefilledOrganisation(any())).thenReturn(orgUnitRestSearchItem);
  }

  @SecurityTest
  void getSearch_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSearch_whenUserDoesHaveTechnicalReviewFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class)
                .getSearch(form, user)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchApplications_whenUserDoesHaveTechnicalReviewFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(SearchController.class)
                .searchApplications(form, user)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getSearch_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class)
                .getSearch(form, user)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchApplications_whenUserDoesHaveProcessFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(SearchController.class)
                .searchApplications(form, user)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getSearch_whenUserDoesHaveAssignFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class)
                .getSearch(form, user)))
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchApplications_whenUserDoesHaveAssignFcsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS)))
        .thenReturn(true);

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
    when(teamService.isRegulatorUser(user)).thenReturn(false);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat(modelAndView)
        .isNull();
  }

  @Test
  void getSearch_RegulatorUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SearchController.class).getSearch(null, null)))
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
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForEditor(null, null)))
        .containsEntry("pageTitle", SEARCH_TITLE);
  }

  @Test
  void searchApplications_RegulatorUser() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(permissionService.hasPermission(user, EnumSet.of(RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)))
        .thenReturn(true);
    when(searchService.getRegulatorSearchResultItems(any(ApplicationDataFilterForm.class), any(ServiceUserDetail.class)))
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
  void clearSearchFilter() throws Exception {
    var expectedRedirectUrl = ReverseRouter.route(on(SearchController.class).getSearch(null, null));

    mockMvc.perform(
            get(ReverseRouter.route(on(SearchController.class).clearSearchFilter(form)))
                .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirectUrl));
  }
}
