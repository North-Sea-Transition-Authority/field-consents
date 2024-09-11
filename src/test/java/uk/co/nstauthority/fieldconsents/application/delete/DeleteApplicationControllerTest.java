package uk.co.nstauthority.fieldconsents.application.delete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = DeleteApplicationController.class)
class DeleteApplicationControllerTest extends AbstractApplicationControllerTest {

  private static final String PAGE_TITLE = "Are you sure you want to delete this draft application?";

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @SecurityTest
  void getDeleteApplication_whenInProgressAndUserHasCreatePermission_thenGetDeleteScreenWithSummaryView() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var viewName = "fcs/application/deleteApplication";
    var modelAndView = new ModelAndView(viewName)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("summarySections", List.of())
        .addObject("accordionId", 123)
        .addObject("wideSummaryDisplay", false)
        .addObject("selectedApplicationVersionView", null)
        .addObject("applicationVersionViews", List.of());

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.CREATE_FCS_APPLICATIONS)).thenReturn(true);
    when(applicationSummaryService.getApplicationSummaryModelAndView(applicationVersion, viewName, PAGE_TITLE, user)).thenReturn(modelAndView);

    mockMvc.perform(get(ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(APPLICATION_ID, user)))
            .with(user(user))
            .with(csrf()))
        .andExpectAll(
            status().isOk(),
            view().name(viewName),
            model().attribute("pageTitle", PAGE_TITLE),
            model().attribute("summarySections", List.of()),
            model().attribute("accordionId", 123),
            model().attribute("wideSummaryDisplay", false),
            model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null)))
        );
  }

  @SecurityTest
  void getDeleteApplication_whenInProgressAndUserHasNoCreatePermission_thenUserIsForbiddenToDelete() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.CREATE_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(APPLICATION_ID, user)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getDeleteApplication_whenSubmittedAndUserHasCreatePermission_thenUserIsForbiddenToDelete() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.CREATE_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(APPLICATION_ID, user)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteApplication_whenInProgressAndUserHasCreatePermission_thenRedirect() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.CREATE_FCS_APPLICATIONS
    )).thenReturn(true);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Draft application has been successfully deleted")
        .build();

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(DeleteApplicationController.class)
            .deleteApplication(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
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
  }

  @Test
  void deleteApplication_whenInProgressAndUserHasNoCreatePermission_thenUserIsForbiddenToDelete() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.CREATE_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DeleteApplicationController.class)
            .deleteApplication(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteApplication_whenSubmittedAndUserHasCreatePermission_thenUserIsForbiddenToDelete() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.CREATE_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(DeleteApplicationController.class)
            .deleteApplication(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }
}
