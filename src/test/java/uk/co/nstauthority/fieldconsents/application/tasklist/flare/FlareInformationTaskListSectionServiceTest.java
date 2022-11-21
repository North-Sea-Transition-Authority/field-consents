package uk.co.nstauthority.fieldconsents.application.tasklist.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARES_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_VENT_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_INFORMATION_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_REPORT_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;

import java.util.ArrayList;
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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class FlareInformationTaskListSectionServiceTest {

  @Mock
  private FlareService flareService;

  @Mock
  private FlareReportPeriodService flareReportPeriodService;

  @Mock
  private FlareReportService flareReportService;

  @Mock
  private ConsentLengthService consentLengthService;

  private FlareInformationTaskListSectionService flareInformationTaskListSectionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    flareInformationTaskListSectionService = new FlareInformationTaskListSectionService(flareService,
        flareReportPeriodService, flareReportService, consentLengthService);
  }

  @Test
  void getSection_whenNotFlareApplication() {
    ApplicationVersion ventApplicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);

    assertThat(flareInformationTaskListSectionService.getSection(ventApplicationVersion)).isEmpty();
  }

  @Test
  void getSection_flareTaskListSection() {
    Optional<TaskListSection> taskListSectionOptional = flareInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, FLARE_INFORMATION_SECTION, FLARE_VENT_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getSection_withNonEmptyListOfFlares() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion))
        .thenReturn(FlareTestUtil.flares);

    Optional<TaskListSection> taskListSectionOptional = flareInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.BLOCKED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withEmptyListOfFlares() {
    Optional<TaskListSection> taskListSectionOptional = flareInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareController.class).addFlare(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.BLOCKED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlaresTaskListItem_notCompleted() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());
    TaskListItem item = flareInformationTaskListSectionService.getFlaresTaskListItem(applicationVersion);

    assertTaskListItem(
        item,
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareController.class).addFlare(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlaresTaskListItem_completed() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(FlareTestUtil.flares);
    TaskListItem item = flareInformationTaskListSectionService.getFlaresTaskListItem(applicationVersion);

    assertTaskListItem(
        item,
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_blocked() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(false);
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.empty());
    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.BLOCKED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_completed() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(new ConsentLengthDetails()));
    when(flareReportService.flareReportComplete(applicationVersion)).thenReturn(true);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(FlareReportController.class)
            .getFlareReportForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_inProgress() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(new ConsentLengthDetails()));
    when(flareReportService.flareReportComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(FlareReportController.class)
            .getFlareReportForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getFlareReportTaskListItem_notCompleted() {
    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(false);
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(new ConsentLengthDetails()));
    when(flareReportService.flareReportComplete(applicationVersion)).thenReturn(false);

    TaskListItem item = flareInformationTaskListSectionService.getFlareReportTaskListItem(applicationVersion);

    assertTaskListItem(item,
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationVersion.getApplication().getId()))
    );
  }

}