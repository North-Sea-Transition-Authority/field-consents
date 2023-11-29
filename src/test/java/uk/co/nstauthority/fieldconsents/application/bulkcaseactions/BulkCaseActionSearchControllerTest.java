package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

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
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSearchController.FORM_SESSION_ATTRIBUTE;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = BulkCaseActionSearchController.class)
class BulkCaseActionSearchControllerTest extends AbstractControllerTest {

  private static final Class<BulkCaseActionSearchController> CONTROLLER_CLASS = BulkCaseActionSearchController.class;
  private static final String VIEW_NAME = "fcs/application/bulk-case-actions/search";

  @MockBean
  private BulkCaseActionService bulkCaseActionService;

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

    var applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());
    when(bulkCaseActionService.getApplicationDataItems(user)).thenReturn(applicationDataItems);
    when(bulkCaseActionService.getSelectedApplicationIds(any(HttpSession.class))).thenReturn(Collections.emptyList());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"))
        .andExpect(model().attribute("pageTitle", BulkCaseActionSearchController.PAGE_TITLE))
        .andExpect(model().attribute("actions", List.of(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)))
        .andExpect(model().attribute("applicationDataItems", applicationDataItems))
        .andExpect(model().attribute("form", BulkCaseActionSearchForm.empty()));
  }

  @Test
  void getSearchResults_withPreviouslySelectedApplications() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var applicationDataItem1 = applicationDataItemBuilderWithDefaults(1).build();
    var applicationDataItem2 = applicationDataItemBuilderWithDefaults(2).build();
    var applicationDataItem3 = applicationDataItemBuilderWithDefaults(3).build();

    var applicationDataItems = List.of(applicationDataItem1, applicationDataItem2, applicationDataItem3);
    when(bulkCaseActionService.getApplicationDataItems(user)).thenReturn(applicationDataItems);
    when(bulkCaseActionService.getSelectedApplicationIds(any(HttpSession.class))).thenReturn(List.of(1, 2, 3));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", BulkCaseActionSearchController.PAGE_TITLE))
        .andExpect(model().attribute("actions", List.of(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)))
        .andExpect(model().attribute("applicationDataItems", applicationDataItems))
        .andExpect(model().attribute("form", new BulkCaseActionSearchForm(List.of("1", "2", "3"))));
  }

  @Test
  void getSearchResults_withPreviouslySelectedApplications_someNoLongerAvailable() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var applicationDataItem = applicationDataItemBuilderWithDefaults(1).build();
    var applicationDataItems = List.of(applicationDataItem);
    when(bulkCaseActionService.getApplicationDataItems(user)).thenReturn(applicationDataItems);
    when(bulkCaseActionService.getSelectedApplicationIds(any(HttpSession.class))).thenReturn(List.of(1, 2, 3));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/search"))
        .andExpect(model().attribute("pageTitle", BulkCaseActionSearchController.PAGE_TITLE))
        .andExpect(model().attribute("actions", List.of(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)))
        .andExpect(model().attribute("applicationDataItems", applicationDataItems))
        // ids 2 and 3 are no longer in the search results, so they can't be selected by default
        .andExpect(model().attribute("form", new BulkCaseActionSearchForm(List.of("1"))));
  }

  @Test
  void submitAssignCaseOfficerSelection() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var session = new MockHttpSession();
    var selectedApplicationIds = List.of("1", "2", "3");

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
        .session(session)
        .with(user(user))
        .with(csrf())
        .param(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER, BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)
        .param("selectedApplicationIds", String.join(", ", selectedApplicationIds)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null))));

    assertThat(session.getAttribute(FORM_SESSION_ATTRIBUTE)).isInstanceOf(BulkCaseActionSearchForm.class);

    var sessionAttribute = (BulkCaseActionSearchForm) session.getAttribute(FORM_SESSION_ATTRIBUTE);
    assertThat(sessionAttribute.selectedApplicationIds()).containsExactlyElementsOf(selectedApplicationIds);
  }

  @Test
  void submitAssignCaseOfficerSelection_overridesPreviousSelection() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var session = new MockHttpSession();
    session.setAttribute(FORM_SESSION_ATTRIBUTE, new BulkCaseActionSearchForm(List.of("7", "8", "9")));

    var selectedApplicationIds = List.of("1", "2", "3");

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
            .session(session)
            .with(user(user))
            .with(csrf())
            .param(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER, BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER)
            .param("selectedApplicationIds", String.join(", ", selectedApplicationIds)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null))));

    assertThat(session.getAttribute(FORM_SESSION_ATTRIBUTE)).isInstanceOf(BulkCaseActionSearchForm.class);

    var sessionAttribute = (BulkCaseActionSearchForm) session.getAttribute(FORM_SESSION_ATTRIBUTE);
    assertThat(sessionAttribute.selectedApplicationIds()).containsExactlyElementsOf(selectedApplicationIds);
  }

  @Test
  void submitAssignCaseOfficerSelection_nothingSelected() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var session = new MockHttpSession();

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS).getSearchResults(null, null)))
            .session(session)
            .with(user(user))
            .with(csrf())
            .param(BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER, BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    assertThat(session.getAttribute(FORM_SESSION_ATTRIBUTE)).isNull();
  }


  private ApplicationDataItem.Builder applicationDataItemBuilderWithDefaults(Integer applicationId) {
    return ApplicationDataItem.newBuilder()
        .withApplicationId(applicationId)
        .withType("")
        .withReference("")
        .withOperator("")
        .withDuration("")
        .withAceFlag("")
        .withAsset("")
        .withGeographicArea("")
        .withStatus("")
        .withCaseOfficer("")
        .withTechnicalReviewer("")
        .withSubmittedDateTime("")
        .withSubmittedBy("");
  }

}
