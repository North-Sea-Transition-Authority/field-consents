package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES_MAP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentController.TECHNICAL_REVIEW_ASSIGNMENT_SUCCESS_MESSAGE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Clock;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = TechnicalReviewAssignmentController.class)
class TechnicalReviewAssignmentControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/review/technicalReviewAssignment";
  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private Clock clock;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private TechnicalReviewService technicalReviewService;

  @MockBean
  private TechnicalReviewAssignmentService technicalReviewAssignmentService;

  @MockBean
  private TechnicalReviewAssignmentFormValidator technicalReviewAssignmentFormValidator;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @SecurityTest
  void getTechnicalReviewAssignment_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CaseAssignmentController.class)
            .getCaseAssignment(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getTechnicalReviewAssignment_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
            .getTechnicalReviewAssignment(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getTechnicalReviewAssignment_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .thenReturn(technicalReview);
    when(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(technicalReview))
        .thenReturn(TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES);
    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
            .getTechnicalReviewAssignment(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getCaseAssignment(ApplicationVersion applicationVersion) throws Exception {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .thenReturn(technicalReview);
    when(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(technicalReview))
        .thenReturn(TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
            .getTechnicalReviewAssignment(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("technicalReviewerAssignmentCandidates", TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null, null))));
  }

  @SecurityTest
  void assignTechnicalReviewer_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
            .assignTechnicalReviewer(APPLICATION_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignTechnicalReviewer_valid(ApplicationVersion applicationVersion) throws Exception {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(technicalReviewAssignmentFormValidator).validate(any(), any());

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_5.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_5);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .thenReturn(technicalReview);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent(TECHNICAL_REVIEW_ASSIGNMENT_SUCCESS_MESSAGE.apply(ENERGY_PORTAL_USER_5.displayName()))
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
                .assignTechnicalReviewer(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("technicalReviewerWuaId", String.valueOf(ENERGY_PORTAL_USER_5.webUserAccountId()))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(technicalReviewAssignmentService, times(1))
        .assignTechnicalReviewer(technicalReview, SERVICE_USER_DETAIL_USER_5, user);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignTechnicalReviewer_valid_whenAssigningToYourself(ApplicationVersion applicationVersion) throws Exception {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(technicalReviewAssignmentFormValidator).validate(any(), any());

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_5.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_5);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .thenReturn(technicalReview);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent(TECHNICAL_REVIEW_ASSIGNMENT_SUCCESS_MESSAGE.apply("yourself"))
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
                .assignTechnicalReviewer(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(SERVICE_USER_DETAIL_USER_5))
                .param("technicalReviewerWuaId", String.valueOf(ENERGY_PORTAL_USER_5.webUserAccountId()))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(technicalReviewAssignmentService, times(1))
        .assignTechnicalReviewer(technicalReview, SERVICE_USER_DETAIL_USER_5, SERVICE_USER_DETAIL_USER_5);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void assignTechnicalReviewer_invalid(ApplicationVersion applicationVersion) throws Exception {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(technicalReviewAssignmentFormValidator).validate(any(), any());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .thenReturn(technicalReview);
    when(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(technicalReview))
        .thenReturn(TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES);

    mockMvc.perform(
            post(ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
                .assignTechnicalReviewer(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("technicalReviewerAssignmentCandidates", TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null, null))));
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
