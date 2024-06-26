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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CASE_OFFICER_ASSIGNMENT_CANDIDATES;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.Set;
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
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = CaseAssignmentController.class)
class CaseAssignmentControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private CaseAssignmentService caseAssignmentService;

  @MockBean
  private CaseAssignmentFormValidator caseAssignmentFormValidator;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @MockBean
  private TeamMemberViewService teamMemberViewService;


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
        .thenReturn(Set.of());

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
    when(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, user))
        .thenReturn(CASE_OFFICER_ASSIGNMENT_CANDIDATES);
    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, CASE_OFFICER_ASSIGN_OWNERSHIP, CASE_OFFICER_REASSIGN_OWNERSHIP))
        .thenReturn(true);

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
    when(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, user))
        .thenReturn(CASE_OFFICER_ASSIGNMENT_CANDIDATES);
    when(teamMemberViewService.getUsersMap(CASE_OFFICER_ASSIGNMENT_CANDIDATES))
        .thenReturn(CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP);

    mockMvc.perform(get(ReverseRouter.route(on(CaseAssignmentController.class)
            .getCaseAssignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseAssignment"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("caseOfficerAssignmentCandidates", CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null))));
  }

  @SecurityTest
  void assignCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .assignCaseOfficer(APPLICATION_ID, null, null, null, null)))
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

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_1.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_1);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("You have assigned this case to %s".formatted(ENERGY_PORTAL_USER_1.displayName()))
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .assignCaseOfficer(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("caseOfficerWuaId", String.valueOf(ENERGY_PORTAL_USER_1.webUserAccountId()))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(caseAssignmentService, times(1))
        .assignCaseOfficer(applicationVersion, SERVICE_USER_DETAIL_USER_1, user);
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
    when(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, user))
        .thenReturn(CASE_OFFICER_ASSIGNMENT_CANDIDATES);
    when(teamMemberViewService.getUsersMap(CASE_OFFICER_ASSIGNMENT_CANDIDATES))
        .thenReturn(CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP);

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .assignCaseOfficer(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseAssignment"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("caseOfficerAssignmentCandidates", CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null))));
  }

  @SecurityTest
  void takeOwnershipCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .takeOwnershipCaseOfficer(APPLICATION_ID, null, null)))
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

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("You have taken ownership of this case")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .takeOwnershipCaseOfficer(APPLICATION_ID, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(caseAssignmentService, times(1))
        .assignCaseOfficer(applicationVersion, user, user);
  }

  @SecurityTest
  void releaseOwnershipCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .releaseOwnershipCaseOfficer(APPLICATION_ID, null, null)))
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

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("You have released ownership of this case")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .releaseOwnershipCaseOfficer(APPLICATION_ID, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(caseAssignmentService, times(1))
        .unassignCaseOfficer(applicationVersion, user);
  }

  @SecurityTest
  void returnToCaseOfficer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CaseAssignmentController.class)
            .returnToCaseOfficer(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void returnToCaseOfficer(ApplicationVersion applicationVersion) throws Exception {
    applicationVersion.setCaseOfficerWuaId(ENERGY_PORTAL_USER_1.webUserAccountId());
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_1.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_1);
    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("You have reassigned %s to %s".formatted(DUMMY_APP_REF, ENERGY_PORTAL_USER_1.displayName()))
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CaseAssignmentController.class)
                .returnToCaseOfficer(APPLICATION_ID, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(caseAssignmentService, times(1))
        .returnToCaseOfficer(applicationVersion, user);
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
