package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;

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
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformation;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationController;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class AdditionalInformationTaskListSectionServiceTest {

  public static final String ADDITIONAL_INFORMATION_SECTION = "Additional information";
  
  public static final int ADDITIONAL_INFORMATION_DISPLAY_ORDER = 30;

  public static final String SUPPORTING_INFORMATION_TASK_LIST_ITEM = "Supporting information";

  private AdditionalInformationTaskListSectionService additionalInformationTaskListSectionService;

  @Mock
  private SupportingInformationService supportingInformationService;

  private ApplicationVersion applicationVersion;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    additionalInformationTaskListSectionService = new AdditionalInformationTaskListSectionService(supportingInformationService);
  }

  @Test
  void getSection_additionalInformationSection() {
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.of(new SupportingInformation()));

    Optional<TaskListSection> taskListSectionOptional = additionalInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, ADDITIONAL_INFORMATION_SECTION, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getSection_withTaskListItemForSupportingInformationNotStarted() {
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.empty());

    Optional<TaskListSection> taskListSectionOptional = additionalInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        SUPPORTING_INFORMATION_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withTaskListItemForSupportingInformationCompleted() {
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.of(new SupportingInformation()));

    Optional<TaskListSection> taskListSectionOptional = additionalInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        SUPPORTING_INFORMATION_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(applicationVersion.getApplication().getId()))
    );
  }
}