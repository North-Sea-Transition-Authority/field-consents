package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateController.REQUEST_PAGE_TITLE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.APPLICATION_UPDATE_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_DATE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_DATE_TIME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationUpdateController.class)
class ApplicationUpdateControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationUpdateService applicationUpdateService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationUpdateRequestFormValidator applicationUpdateRequestFormValidator;

  @MockBean
  private Clock clock;

  @SecurityTest
  void getApplicationUpdateRequest_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationUpdateController.class)
            .getApplicationUpdateRequest(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getApplicationUpdateRequest_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Collections.emptyList());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationUpdateController.class)
            .getApplicationUpdateRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationUpdateRequest_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationUpdateService.getApplicationUpdateRequestForm(applicationVersion))
        .thenReturn(new ApplicationUpdateRequestForm());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any(), any());

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(APPLICATION_UPDATE_REQUEST));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationUpdateController.class)
            .getApplicationUpdateRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/update/applicationUpdateRequest"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationUpdateRequest(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationUpdateService.getApplicationUpdateRequestForm(applicationVersion))
        .thenReturn(new ApplicationUpdateRequestForm());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any(), any());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationUpdateController.class)
            .getApplicationUpdateRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/update/applicationUpdateRequest"))
        .andExpect(model().attribute("pageTitle", REQUEST_PAGE_TITLE))
        .andExpect(model().attributeExists("summarySections"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("wideSummaryDisplay",
            ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType())))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(APPLICATION_ID, null))));
  }

  @SecurityTest
  void sendApplicationUpdateRequest_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationUpdateController.class)
            .sendApplicationUpdateRequest(APPLICATION_ID, null, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void sendApplicationUpdateRequest_valid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Application update request sent to operator")
        .build();

    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var deadlineDateStr = DateUtils.format(CURRENT_DATE, DateUtils.DATE_PICKER_FORMAT);
    var deadlineHoursStr = String.valueOf(CURRENT_DATE_TIME.plusHours(DEADLINE_AHEAD_HOURS).getHour());
    var deadlineMinutesStr = String.valueOf(CURRENT_DATE_TIME.getMinute());
    var deadlineInstant =
        DateUtils.datePickerWithTimeStringToInstant(deadlineDateStr, deadlineHoursStr, deadlineMinutesStr, clock);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationUpdateController.class)
                .sendApplicationUpdateRequest(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
                .param("deadlineDate", deadlineDateStr)
                .param("deadlineHours", deadlineHoursStr)
                .param("deadlineMinutes", deadlineMinutesStr)
                .param("requestText", APPLICATION_UPDATE_REQUEST_TEXT)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(applicationUpdateService, times(1))
        .saveApplicationUpdateRequest(applicationVersion, deadlineInstant,
            APPLICATION_UPDATE_REQUEST_TEXT, user);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void sendApplicationUpdateRequest_invalid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(applicationUpdateRequestFormValidator).validate(any(), any());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationUpdateController.class)
                .sendApplicationUpdateRequest(APPLICATION_ID, null, null, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/update/applicationUpdateRequest"))
        .andExpect(model().attribute("pageTitle", REQUEST_PAGE_TITLE))
        .andExpect(model().attributeExists("summarySections"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("wideSummaryDisplay",
            ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType())))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(APPLICATION_ID, null))));
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
