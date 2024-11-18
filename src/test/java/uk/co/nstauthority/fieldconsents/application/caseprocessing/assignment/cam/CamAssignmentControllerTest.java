package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_ASSIGNMENT_CANDIDATES;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_ASSIGNMENT_CANDIDATES_MAP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentIssuingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = CamAssignmentController.class)
class CamAssignmentControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private CamAssignmentService camAssignmentService;

  @MockBean
  private CamAssignmentFormValidator camAssignmentFormValidator;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @SecurityTest
  void getCamAssignment_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamAssignment(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getCamAssignment_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamAssignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getCamAssignment_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES);
    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, CAM_ASSIGN_OWNERSHIP))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamAssignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/camAssignment"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getCamAssignment(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES);
    when(teamMemberViewService.getUsersMap(CAM_USER_ASSIGNMENT_CANDIDATES))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES_MAP);

    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamAssignment(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/camAssignment"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("camUserAssignmentCandidates", CAM_USER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID, null))));
  }

  @SecurityTest
  void assignCamUser_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CamAssignmentController.class)
            .assignCamUser(APPLICATION_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignCamUser_valid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    doCallRealMethod().when(camAssignmentFormValidator).validate(any(), any());

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_1.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_1);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("You have assigned %s to %s".formatted(DUMMY_APP_REF, ENERGY_PORTAL_USER_1.displayName()))
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CamAssignmentController.class)
                .assignCamUser(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("camWuaId", String.valueOf(ENERGY_PORTAL_USER_1.webUserAccountId()))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(camAssignmentService, times(1))
        .assignCamUser(applicationVersion, SERVICE_USER_DETAIL_USER_1, user);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignCamUser_invalid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(camAssignmentFormValidator).validate(any(), any());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES);
    when(teamMemberViewService.getUsersMap(CAM_USER_ASSIGNMENT_CANDIDATES))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES_MAP);

    mockMvc.perform(
            post(ReverseRouter.route(on(CamAssignmentController.class)
                .assignCamUser(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/camAssignment"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("camUserAssignmentCandidates", CAM_USER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID, null))));
  }

  @SecurityTest
  void getCamReassignment_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamReassignment(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getCamReassignment_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamReassignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getCamReassignment_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES);
    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, CAM_REASSIGN_OWNERSHIP))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamReassignment(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/camAssignment"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getCamReassignment(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES);
    when(teamMemberViewService.getUsersMap(CAM_USER_ASSIGNMENT_CANDIDATES))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES_MAP);

    mockMvc.perform(get(ReverseRouter.route(on(CamAssignmentController.class)
            .getCamReassignment(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/camAssignment"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("camUserAssignmentCandidates", CAM_USER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null))));
  }

  @SecurityTest
  void reassignCamUser_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CamAssignmentController.class)
            .reassignCamUser(APPLICATION_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void reassignCamUser_valid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    doCallRealMethod().when(camAssignmentFormValidator).validate(any(), any());

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_1.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_1);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("You have assigned %s to %s".formatted(DUMMY_APP_REF, ENERGY_PORTAL_USER_1.displayName()))
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(CamAssignmentController.class)
                .reassignCamUser(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("camWuaId", String.valueOf(ENERGY_PORTAL_USER_1.webUserAccountId()))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(camAssignmentService, times(1))
        .assignCamUser(applicationVersion, SERVICE_USER_DETAIL_USER_1, user);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void reassignCamUser_invalid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(camAssignmentFormValidator).validate(any(), any());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, user))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES);
    when(teamMemberViewService.getUsersMap(CAM_USER_ASSIGNMENT_CANDIDATES))
        .thenReturn(CAM_USER_ASSIGNMENT_CANDIDATES_MAP);

    mockMvc.perform(
            post(ReverseRouter.route(on(CamAssignmentController.class)
                .reassignCamUser(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/camAssignment"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("camUserAssignmentCandidates", CAM_USER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null))));
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
