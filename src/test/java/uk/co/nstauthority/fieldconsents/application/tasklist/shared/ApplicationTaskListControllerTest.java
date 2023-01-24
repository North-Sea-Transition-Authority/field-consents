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

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextJson;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil;

@ContextConfiguration(classes = ApplicationTaskListController.class)
class ApplicationTaskListControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ApplicationTaskListService applicationTaskListService;

  @MockBean
  private ApplicationContextService applicationContextService;

  private List<TaskListSection> flareTaskListSections;

  private ApplicationContextJson applicationContext;

  @BeforeEach
  void setUp() {
    flareTaskListSections = TaskListTestUtil.getFlareTaskListSectionWithItems(APPLICATION_ID);
    applicationContext = new ApplicationContextJson(FieldTestUtil.field1Json,
        OrganisationUnitTestUtil.orgUnit1Json);
  }

  @Test
  @WithMockUser
  void getTaskList_withFlareApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationContextService.getApplicationContextJson(applicationVersion)).thenReturn(applicationContext);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
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
  @WithMockUser
  void getTaskList_withVentApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContextJson(applicationVersion)).thenReturn(applicationContext);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
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
  @WithMockUser
  void getTaskList_withProductionApplication() throws Exception {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(flareTaskListSections);
    when(applicationContextService.getApplicationContextJson(applicationVersion)).thenReturn(applicationContext);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
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
}