package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.applicationUpdateRequestView;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil;

@ContextConfiguration(classes = ApplicationTaskListController.class)
class ApplicationTaskListControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationTaskListService applicationTaskListService;

  @MockBean
  private ApplicationContextService applicationContextService;

  @MockBean
  private ApplicationUpdateService applicationUpdateService;

  @MockBean
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @MockBean
  private ApplicationService applicationService;

  private List<TaskListSection> flareTaskListSections;

  private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    flareTaskListSections = TaskListTestUtil.getFlareTaskListSectionWithItems(APPLICATION_ID);
    applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(FieldTestUtil.field1Json)
        .withPrimaryOperator("Primary operator")
        .withApplicationVersionStatus(ApplicationVersionStatus.IN_PROGRESS)
        .build();
  }

  @SecurityTest
  void getTaskList_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getTaskList_withFlareApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("pageTitle", "Flare application"),
            entry("applicationContext", applicationContext)
        )
        .containsKey("taskListSections");
  }

  @Test
  void getTaskList_withVentApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("pageTitle", "Vent application"),
            entry("applicationContext", applicationContext)
        )
        .containsKey("taskListSections");
  }

  @Test
  void getTaskList_withProductionApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("pageTitle", "Production application"),
            entry("applicationContext", applicationContext)
        )
        .containsKey("taskListSections");
  }

  @Test
  void getTaskList_withApplicationUpdateInProgress() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(true);
    when(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .thenReturn(applicationUpdateRequestView);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationTaskList"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("pageTitle", "Production application"),
            entry("applicationContext", applicationContext)
        )
        .containsEntry("applicationUpdateRequestView", applicationUpdateRequestView)
        .containsKey("taskListSections");
  }
}
