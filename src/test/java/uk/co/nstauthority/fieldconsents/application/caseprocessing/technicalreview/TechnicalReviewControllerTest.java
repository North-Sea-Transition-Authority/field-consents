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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_DATE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_DATE_TIME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Clock;
import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.summary.TechnicalReviewSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = TechnicalReviewController.class)
class TechnicalReviewControllerTest extends AbstractApplicationControllerTest {

  private static final String TECHNICAL_REVIEWS_VIEW_NAME = "fcs/application/review/technicalReviews";
  private static final String TECHNICAL_REVIEW_REQUEST_VIEW_NAME = "fcs/application/review/technicalReviewRequest";
  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private TechnicalReviewService technicalReviewService;

  @MockBean
  private TechnicalReviewAssignmentService technicalReviewAssignmentService;

  @MockBean
  private TechnicalReviewRequestFormValidator technicalReviewRequestFormValidator;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @MockBean
  private TechnicalReviewSummaryService technicalReviewSummaryService;

  @MockBean
  private Clock clock;

  @SecurityTest
  void getTechnicalReviews_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviews(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getTechnicalReviews_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviews(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getTechnicalReviews_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewSummaryService.getTechnicalReviewSummaryItems(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        TECHNICAL_REVIEWS
    )).thenReturn(true);;

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviews(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(TECHNICAL_REVIEWS_VIEW_NAME));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getTechnicalReviews(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewSummaryService.getTechnicalReviewSummaryItems(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());
    when(applicationService.getApplicationReference(any(ApplicationVersion.class), any(String.class)))
        .thenReturn(DUMMY_APP_REF);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviews(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(TECHNICAL_REVIEWS_VIEW_NAME))
        .andExpect(model().attribute("captionTitle", DUMMY_APP_REF))
        .andExpect(model().attribute("technicalReviewSummaryItems", Collections.emptyList()))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null, null))));
  }

  @SecurityTest
  void getTechnicalReviewRequest_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviewRequest(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getTechnicalReviewRequest_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviewRequest(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getTechnicalReviewRequest_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .thenReturn(new TechnicalReviewRequestForm());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        TECHNICAL_REVIEW_REQUEST
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviewRequest(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(TECHNICAL_REVIEW_REQUEST_VIEW_NAME));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getTechnicalReviewRequest(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .thenReturn(new TechnicalReviewRequestForm());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates())
        .thenReturn(TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES);

    mockMvc.perform(get(ReverseRouter.route(on(TechnicalReviewController.class)
            .getTechnicalReviewRequest(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(TECHNICAL_REVIEW_REQUEST_VIEW_NAME))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("technicalReviewerAssignmentCandidates",
            TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(TechnicalReviewController.class)
                .getTechnicalReviews(APPLICATION_ID, null))));
  }

  @SecurityTest
  void startTechnicalReview_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(TechnicalReviewController.class)
            .startTechnicalReview(APPLICATION_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void startTechnicalReview_valid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(ENERGY_PORTAL_USER_5.webUserAccountId())))
        .thenReturn(ENERGY_PORTAL_USER_5);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Technical review sent to %s".formatted(ENERGY_PORTAL_USER_5.displayName()))
        .build();

    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var deadlineDateStr = DateUtils.format(CURRENT_DATE, DateUtils.DATE_PICKER_FORMAT);
    var deadlineHoursStr = String.valueOf(CURRENT_DATE_TIME.plusHours(DEADLINE_AHEAD_HOURS).getHour());
    var deadlineMinutesStr = String.valueOf(CURRENT_DATE_TIME.getMinute());
    var deadlineInstant =
        DateUtils.datePickerWithTimeStringToInstant(deadlineDateStr, deadlineHoursStr, deadlineMinutesStr);

    mockMvc.perform(
            post(ReverseRouter.route(on(TechnicalReviewController.class)
                .startTechnicalReview(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("technicalReviewerWuaId", String.valueOf(ENERGY_PORTAL_USER_5.webUserAccountId()))
                .param("deadlineDate", deadlineDateStr)
                .param("deadlineHours", deadlineHoursStr)
                .param("deadlineMinutes", deadlineMinutesStr)
                .param("requestText", TECHNICAL_REVIEW_REQUEST_TEXT)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(technicalReviewService, times(1))
        .saveTechnicalReviewRequest(applicationVersion, deadlineInstant,
            TECHNICAL_REVIEW_REQUEST_TEXT, SERVICE_USER_DETAIL_USER_5, user);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void startTechnicalReview_invalid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(technicalReviewRequestFormValidator).validate(any(), any());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates())
        .thenReturn(TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES);

    mockMvc.perform(
            post(ReverseRouter.route(on(TechnicalReviewController.class)
                .startTechnicalReview(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(TECHNICAL_REVIEW_REQUEST_VIEW_NAME))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("technicalReviewerAssignmentCandidates",
            TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES_MAP))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(TechnicalReviewController.class)
                .getTechnicalReviews(APPLICATION_ID, null))));
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
