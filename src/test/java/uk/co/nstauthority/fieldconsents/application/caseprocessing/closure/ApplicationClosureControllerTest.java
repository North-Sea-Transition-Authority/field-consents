package uk.co.nstauthority.fieldconsents.application.caseprocessing.closure;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationClosureController.class)
class ApplicationClosureControllerTest extends AbstractApplicationControllerTest {

  private static final ApplicationVersion APPLICATION_VERSION = ApplicationTestUtil
      .getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  private static final int APPLICATION_ID = APPLICATION_VERSION.getId();
  protected ServiceUserDetail user;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationService applicationService;

  @BeforeEach
  void setUp() {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(APPLICATION_VERSION)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(APPLICATION_VERSION);
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @SecurityTest
  void getConfirmation_noUser_returnToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationClosureController.class)
            .getConfirmation(APPLICATION_ID, user))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getConfirmation_hasNoPermission_forbidden() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationClosureController.class)
            .getConfirmation(APPLICATION_ID, user)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void closeApplication_noUser_returnToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationClosureController.class)
            .closeApplication(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void closeApplication_hasNoPermission_forbidden() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationClosureController.class)
            .closeApplication(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getConfirmation_withPermission_success() throws Exception {
    var viewName = "fcs/application/closureForm";
    var pageTitle = "Are you sure you want to close this application?";

    var modelAndView = new ModelAndView(viewName)
        .addObject("pageTitle", pageTitle)
        .addObject("summarySections", List.of())
        .addObject("accordionId", 123)
        .addObject("wideSummaryDisplay", false)
        .addObject("selectedApplicationVersionView", null)
        .addObject("applicationVersionViews", List.of());

    var applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(new Application(APPLICATION_ID));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationSummaryService.getApplicationSummaryModelAndView(applicationVersion, viewName, pageTitle, user)).thenReturn(modelAndView);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationClosureController.class)
            .getConfirmation(APPLICATION_ID, user)))
            .with(user(user)))
        .andExpect(view().name(viewName))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "closureUrl",
            ReverseRouter.route(on(ApplicationClosureController.class).closeApplication(APPLICATION_ID, null)))
        )
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(APPLICATION_ID, null, null, null)))
        );
  }

  @Test
  void closeApplication_withPermission_success() throws Exception {
    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Application CASE_REF has been successfully closed")
        .build();

    when(applicationService.generateApplicationReference(APPLICATION_VERSION))
        .thenReturn("CASE_REF");
    when(applicationVersionService.getAllNonDeletedApplicationVersionsByApplicationId(APPLICATION_ID))
        .thenReturn(List.of(APPLICATION_VERSION));

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ApplicationClosureController.class)
            .closeApplication(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))))
        .andReturn();

    var actualNotificationBanner = (NotificationBanner) modelAndView.getFlashMap().get("flash");
    assertThat(actualNotificationBanner)
        .extracting(
            NotificationBanner::getTitle,
            NotificationBanner::getHeadingContent,
            NotificationBanner::getOtherContent,
            NotificationBanner::getType
        ).containsExactly(
            expectedNotificationBanner.getTitle(),
            expectedNotificationBanner.getHeadingContent(),
            expectedNotificationBanner.getOtherContent(),
            expectedNotificationBanner.getType()
        );
    verify(applicationVersionService)
      .closeApplicationVersion(APPLICATION_VERSION);
  }
}
