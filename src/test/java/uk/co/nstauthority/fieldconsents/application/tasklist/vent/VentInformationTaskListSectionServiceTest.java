package uk.co.nstauthority.fieldconsents.application.tasklist.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_VENT_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.VENTS_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.VENT_INFORMATION_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.vent.Vent;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class VentInformationTaskListSectionServiceTest {

  @Mock
  private VentService ventService;

  private VentInformationTaskListSectionService ventInformationTaskListSectionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    ventInformationTaskListSectionService = new VentInformationTaskListSectionService(ventService);
  }

  @Test
  void getSection_whenNotVentApplication() {
    ApplicationVersion ventApplicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

    assertThat(ventInformationTaskListSectionService.getSection(ventApplicationVersion)).isEmpty();
  }

  @Test
  void getSection_flareTaskListSection() {
    Optional<TaskListSection> taskListSectionOptional = ventInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, VENT_INFORMATION_SECTION, FLARE_VENT_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getSection_withNonEmptyListOfVents() {
    Vent ventLp = new Vent(applicationVersion, 5, VentType.HP_VENT,"LP DESCRIPTION", Boolean.TRUE, "LP COMMENT");
    when(ventService.getVentsForApplicationVersion(applicationVersion))
        .thenReturn(Collections.singletonList(ventLp));

    Optional<TaskListSection> taskListSectionOptional = ventInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems,
        0,
        VENTS_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withEmptyListOfVents() {
    Optional<TaskListSection> taskListSectionOptional = ventInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems,
        0,
        VENTS_TASK_LIST_ITEM,
        TaskListLabel.NOT_COMPLETED,
        ReverseRouter.route(on(VentController.class).addVent(applicationVersion.getApplication().getId()))
    );
  }
}