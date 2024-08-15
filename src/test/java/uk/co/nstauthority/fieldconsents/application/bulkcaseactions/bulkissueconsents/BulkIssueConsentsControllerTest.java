package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSelectedApplicationsForm;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.task.BulkIssueConsentsTaskService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = BulkIssueConsentsController.class)
class BulkIssueConsentsControllerTest extends AbstractControllerTest {

  private static final String SESSION_ATTRIBUTE = "bulkCaseActions-issueConsents";
  private static final String BULK_ISSUE_CONSENTS = "Bulk issue consents";

  @MockBean
  private BulkIssueConsentsFormValidator validator;

  @MockBean
  private BulkIssueConsentsTaskService bulkIssueConsentsTaskService;

  @MockBean
  private BulkCaseActionService bulkCaseActionService;

  @Autowired
  private BulkIssueConsentsController bulkIssueConsentsController;

  private MockHttpSession httpSession;

  private BulkCaseActionSelectedApplicationsForm bulkCaseActionSelectedApplicationsForm;

  private BulkIssueConsentsSessionContext bulkIssueConsentsSessionContext;

  @BeforeEach
  void setUp() {
    this.httpSession = new MockHttpSession();
    this.bulkCaseActionSelectedApplicationsForm = new BulkCaseActionSelectedApplicationsForm();
    this.bulkIssueConsentsSessionContext = new BulkIssueConsentsSessionContext(
        httpSession,
        BulkIssueConsentsSearchFiltersForm.empty(),
        bulkCaseActionSelectedApplicationsForm
    );
    this.httpSession.setAttribute(SESSION_ATTRIBUTE, this.bulkIssueConsentsSessionContext);
  }

  @SecurityTest
  void viewSelectedApplications() throws Exception {
    var selectedApplicationIds = Set.of(1, 2, 3);
    this.bulkCaseActionSelectedApplicationsForm.setSelectedApplicationIds(selectedApplicationIds);

    when(permissionService.hasPermission(user, Set.of(RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);

    var applicationDataItemView = mock(ApplicationDataItemView.class);
    // these are called in nested freemarker component
    when(applicationDataItemView.reference()).thenReturn("PCON/8000/0 (Version 1)");
    when(applicationDataItemView.url()).thenReturn("/view-application");

    var applicationDataItemViews = List.of(applicationDataItemView);
    when(bulkCaseActionService.getSelectedApplicationDataItemViews(selectedApplicationIds, user)).thenReturn(applicationDataItemViews);

    mockMvc.perform(get(ReverseRouter.route(on(BulkIssueConsentsController.class).viewSelectedApplications(null, null)))
        .session(httpSession)
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/issueConsents"))
        .andExpect(model().attribute("form", BulkIssueConsentsForm.empty()))
        .andExpect(model().attribute("pageTitle", BULK_ISSUE_CONSENTS))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews))
        .andExpect(model().attributeExists("captionHeadingFunction"));
  }

  @Test
  void bulkIssueConsents() throws Exception {
    var selectedApplicationIds = Set.of(1, 2, 3);
    this.bulkCaseActionSelectedApplicationsForm.setSelectedApplicationIds(selectedApplicationIds);
    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).isEqualTo(selectedApplicationIds);

    when(permissionService.hasPermission(user, Set.of(RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);

    var bulkIssueConsentsForm = new BulkIssueConsentsForm(Set.of("1", "2", "3"));

    var applicationVersions = List.of(new ApplicationVersion());
    when(applicationVersionService.getLatestApplicationVersions(Set.of(1, 2, 3))).thenReturn(applicationVersions);

    mockMvc.perform(post(ReverseRouter.route(on(BulkIssueConsentsController.class).bulkIssueConsents(null, null, null, null)))
            .session(httpSession)
            .with(user(user))
            .with(csrf())
            .flashAttr("form", bulkIssueConsentsForm))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))));

    verify(validator).validate(eq(bulkIssueConsentsForm), any(BindingResult.class));

    verify(bulkIssueConsentsTaskService).queueApplicationsForConsentIssue(applicationVersions, user);
    assertThat(this.bulkIssueConsentsSessionContext.getSelectedApplicationsForm().getSelectedApplicationIds()).isEmpty();
  }

  @Test
  void bulkIssueConsents_validationError() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);

    var bulkIssueConsentsForm = new BulkIssueConsentsForm(Set.of("1", "2", "3"));

    doAnswer(invocation -> {
      invocation.getArgument(1, Errors.class).rejectValue("selectedApplicationIds", "invalid", "this is invalid");
      return null;
    })
        .when(validator)
        .validate(eq(bulkIssueConsentsForm), any(BindingResult.class));

    var applicationVersions = List.of(new ApplicationVersion());
    when(applicationVersionService.getLatestApplicationVersions(Set.of(1, 2, 3))).thenReturn(applicationVersions);

    mockMvc.perform(post(ReverseRouter.route(on(BulkIssueConsentsController.class).bulkIssueConsents(null, null, null, null)))
            .session(httpSession)
            .with(user(user))
            .with(csrf())
            .flashAttr("form", bulkIssueConsentsForm))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/issueConsents"));

    verify(bulkIssueConsentsTaskService, never()).queueApplicationsForConsentIssue(any(), any());
  }

  @Test
  void captionHeadingFunction() {
    var applicationDataItemView = mock(ApplicationDataItemView.class);
    when(applicationDataItemView.aceFlag()).thenReturn(true);
    when(applicationDataItemView.operator()).thenReturn("operator");
    when(applicationDataItemView.asset()).thenReturn("asset");

    assertThat(bulkIssueConsentsController.captionHeadingFunction(applicationDataItemView))
        .isEqualTo("%s. operator - asset".formatted(AceFlagStatus.ACE.getDisplayName()));
  }
}