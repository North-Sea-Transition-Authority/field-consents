package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
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
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = CaseAssignmentController.class)
class CaseAssignmentControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  private static final Map<String, String> CASE_OFFICER_CANDIDATES =
      Map.of("1", "user_a", "2", "user_b", "3", "user_c");

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private CaseAssignmentService caseAssignmentService;

  @MockBean
  private CaseAssignmentFormValidator caseAssignmentFormValidator;

  @SecurityTest
  void getCaseAssignment_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CaseAssignmentController.class)
            .getCaseAssignment(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getCaseAssignment_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Collections.emptyList());

    mockMvc.perform(get(ReverseRouter.route(on(CaseAssignmentController.class)
            .getCaseAssignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getCaseAssignment_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(caseAssignmentService.getCaseOfficerCandidates(user))
        .thenReturn(CASE_OFFICER_CANDIDATES);

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CASE_OFFICER_ASSIGN_OWNERSHIP));

    mockMvc.perform(get(ReverseRouter.route(on(CaseAssignmentController.class)
            .getCaseAssignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseAssignment"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getCaseAssignment(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(caseAssignmentService.getCaseOfficerCandidates(user))
        .thenReturn(CASE_OFFICER_CANDIDATES);

    mockMvc.perform(get(ReverseRouter.route(on(CaseAssignmentController.class)
            .getCaseAssignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseAssignment"))
        .andExpect(model().attribute("pageTitle", DUMMY_APP_REF))
        .andExpect(model().attribute("caseOfficerCandidates", CASE_OFFICER_CANDIDATES))
        .andExpect(model().attribute("assignCaseOfficerUrl",
                ReverseRouter.route(on(CaseAssignmentController.class)
                    .assignCaseOfficer(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(APPLICATION_ID, null))));
  }

  @SecurityTest
  void assignCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .assignCaseOfficer(APPLICATION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignCaseOfficer_valid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(caseAssignmentFormValidator).validate(any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .assignCaseOfficer(APPLICATION_ID, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("caseOfficerWuaId", "1")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))));

    verify(caseAssignmentService, times(1))
        .assignCaseOfficer(applicationVersion, WebUserAccountId.from(user));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignCaseOfficer_invalid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(caseAssignmentFormValidator).validate(any(), any());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(caseAssignmentService.getCaseOfficerCandidates(user))
        .thenReturn(CASE_OFFICER_CANDIDATES);

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .assignCaseOfficer(APPLICATION_ID, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseAssignment"))
        .andExpect(model().attribute("pageTitle", DUMMY_APP_REF))
        .andExpect(model().attribute("caseOfficerCandidates", CASE_OFFICER_CANDIDATES))
        .andExpect(model().attribute("assignCaseOfficerUrl",
            ReverseRouter.route(on(CaseAssignmentController.class)
                .assignCaseOfficer(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(APPLICATION_ID, null))));
  }

  @SecurityTest
  void takeOwnershipCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .takeOwnershipCaseOfficer(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void takeOwnershipCaseOfficer(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .takeOwnershipCaseOfficer(APPLICATION_ID, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))));

    verify(caseAssignmentService, times(1))
        .assignCaseOfficer(applicationVersion, WebUserAccountId.from(user));
  }

  @SecurityTest
  void releaseOwnershipCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .releaseOwnershipCaseOfficer(APPLICATION_ID)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void releaseOwnershipCaseOfficer(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .releaseOwnershipCaseOfficer(APPLICATION_ID)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))));

    verify(caseAssignmentService, times(1))
        .unassignCaseOfficer(applicationVersion);
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
