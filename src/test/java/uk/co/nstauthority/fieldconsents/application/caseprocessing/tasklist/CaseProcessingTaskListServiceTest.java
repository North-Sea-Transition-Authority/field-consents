package uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListSection.CASE_TASKS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListSection.OPTIONAL_CASE_TASKS;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class CaseProcessingTaskListServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private CaseProcessingActionService caseProcessingActionService;

  @Spy
  @InjectMocks
  private CaseProcessingTaskListService caseProcessingTaskListService;

  private ApplicationVersion applicationVersion;

  private List<CaseProcessingActionItem> actionItems;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    actionItems = EnumSet.allOf(CaseProcessingActionItem.class).stream().toList();
  }

  @ParameterizedTest
  @EnumSource(CaseProcessingTaskListSection.class)
  void getTaskListSections_singleSection(CaseProcessingTaskListSection caseProcessingTaskListSection) {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, USER)).thenReturn(actionItems);

    var groupedActionItems = Map.of(caseProcessingTaskListSection, actionItems);
    when(caseProcessingActionService.groupActionItemsByTaskListSection(actionItems)).thenReturn(groupedActionItems);

    var taskListSection = mock(TaskListSection.class);
    doReturn(taskListSection)
        .when(caseProcessingTaskListService)
        .getTaskListSection(applicationVersion, caseProcessingTaskListSection, actionItems);

    assertThat(caseProcessingTaskListService.getTaskListSections(applicationVersion, USER)).containsExactly(taskListSection);
  }

  @Test
  void getTaskListSections_multipleSections_inCorrectOrder() {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, USER)).thenReturn(actionItems);

    var groupedActionItems = Map.of(CASE_TASKS, actionItems, OPTIONAL_CASE_TASKS, actionItems);
    when(caseProcessingActionService.groupActionItemsByTaskListSection(actionItems)).thenReturn(groupedActionItems);

    var requiredTaskListSection = mock(TaskListSection.class);
    when(requiredTaskListSection.displayOrder()).thenReturn(2);
    doReturn(requiredTaskListSection)
        .when(caseProcessingTaskListService)
        .getTaskListSection(applicationVersion, CASE_TASKS, actionItems);

    var optionalTaskListSection = mock(TaskListSection.class);
    when(optionalTaskListSection.displayOrder()).thenReturn(1);
    doReturn(optionalTaskListSection)
        .when(caseProcessingTaskListService)
        .getTaskListSection(applicationVersion, OPTIONAL_CASE_TASKS, actionItems);

    assertThat(caseProcessingTaskListService.getTaskListSections(applicationVersion, USER))
        .containsExactly(optionalTaskListSection, requiredTaskListSection);
  }

  @ParameterizedTest
  @EnumSource(CaseProcessingActionItem.class)
  void getTaskListSection_singleItem(CaseProcessingActionItem actionItem) {
    var taskListItem = mock(TaskListItem.class);
    doReturn(taskListItem)
        .when(caseProcessingTaskListService)
        .getTaskListItemFromAction(applicationVersion, actionItem);

    assertThat(caseProcessingTaskListService.getTaskListSection(applicationVersion, CASE_TASKS, Collections.singleton(actionItem)))
        .isEqualTo(new TaskListSection(
            CASE_TASKS.getDisplayName(),
            CASE_TASKS.getDisplayOrder(),
            Collections.singletonList(taskListItem)
        ));
  }

  @Test
  void getTaskListSection_multipleItems() {
    var actionItems = Set.of(CASE_OFFICER_WITHDRAWAL_RESPONSE, APPLICATION_UPDATE_REQUEST, REGULATOR_ADD_CASE_NOTE);

    assertThat(caseProcessingTaskListService.getTaskListSection(applicationVersion, CASE_TASKS, actionItems))
        .satisfies(taskListSection -> {
          assertThat(taskListSection.displayName()).isEqualTo(CASE_TASKS.getDisplayName());
          assertThat(taskListSection.displayOrder()).isEqualTo(CASE_TASKS.getDisplayOrder());
          assertThat(taskListSection.items()).extracting(TaskListItem::displayName)
              .containsExactly(
                  CASE_OFFICER_WITHDRAWAL_RESPONSE.getDisplayName(),
                  APPLICATION_UPDATE_REQUEST.getDisplayName(),
                  REGULATOR_ADD_CASE_NOTE.getDisplayName()
              );
        });
  }

}
