package uk.co.nstauthority.fieldconsents.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;


public class TaskListTestUtil {

  public static final String CONSENT_DETAILS_SECTION = "Consent details";
  public static final String FLARE_INFORMATION_SECTION = "Flare information";

  public static final int CONSENT_DETAILS_DISPLAY_ORDER = 10;

  public static final int FLARE_INFORMATION_DISPLAY_ORDER = 20;

  public static final String CONSENT_LENGTH_TASK_LIST_ITEM = "Consent length";

  public static final String FLARES_TASK_LIST_ITEM = "Flares";

  public static final String FLARE_REPORT_TASK_LIST_ITEM = "Flare report";

  public static void assertTaskListSection(TaskListSection taskListSection, String name, int order) {
    assertThat(taskListSection.displayName()).isEqualTo(name);
    assertThat(taskListSection.displayOrder()).isEqualTo(order);
  }

  public static void assertTaskListItem(List<TaskListItem> taskListItems, int index, String name, TaskListLabel label, String route) {
    TaskListItem item = taskListItems.get(index);
    assertThat(item.displayName()).isEqualTo(name);
    assertThat(item.label()).isEqualTo(label);
    assertThat(item.actionUrl()).isEqualTo(route);
  }

  public static TaskListSection getConsentDetailsTaskListSection(List<TaskListItem> items) {
    return new TaskListSection(CONSENT_DETAILS_SECTION, CONSENT_DETAILS_DISPLAY_ORDER, items);
  }

  public static TaskListSection getFlareInformationTaskListSection(List<TaskListItem> items) {
    return new TaskListSection(FLARE_INFORMATION_SECTION, FLARE_INFORMATION_DISPLAY_ORDER, items);
  }

  public static List<TaskListItem> getConsentDetailsTaskListItems(int applicationId) {
    List<TaskListItem> taskListItems = new ArrayList<>();
    TaskListItem consentLengthTaskListItem = new TaskListItem(CONSENT_LENGTH_TASK_LIST_ITEM,
        TaskListLabel.NOT_COMPLETED,
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationId))
    );
    taskListItems.add(consentLengthTaskListItem);
    return taskListItems;
  }

  public static List<TaskListItem> getFlareInformationTaskListItems(int applicationId) {
    List<TaskListItem> taskListItems = new ArrayList<>();
    TaskListItem flaresTaskListItem = new TaskListItem(
        FLARES_TASK_LIST_ITEM,
        TaskListLabel.NOT_COMPLETED,
        ReverseRouter.route(on(FlareController.class).addFlare(applicationId))
    );
    TaskListItem flareReportTaskListItem = new TaskListItem(
        FLARE_REPORT_TASK_LIST_ITEM,
        TaskListLabel.NOT_COMPLETED,
        // TODO: FCS-189: Update this with route of Flare report screen
        ReverseRouter.route(on(FlareController.class).addFlare(applicationId))
    );
    taskListItems.add(flaresTaskListItem);
    taskListItems.add(flareReportTaskListItem);
    return taskListItems;
  }

  public static List<TaskListSection> getFlareTaskListSectionWithItems(int applicationId) {
    List<TaskListSection> taskListSections = new ArrayList<>();
    List<TaskListItem> consentDetailsTaskListItems = getConsentDetailsTaskListItems(applicationId);
    List<TaskListItem> flareInformationTaskListItems = getFlareInformationTaskListItems(applicationId);

    taskListSections.add(getConsentDetailsTaskListSection(consentDetailsTaskListItems));
    taskListSections.add(getFlareInformationTaskListSection(flareInformationTaskListItems));

    return taskListSections;
  }
}
