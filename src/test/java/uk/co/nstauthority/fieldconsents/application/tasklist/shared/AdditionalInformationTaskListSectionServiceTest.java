package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.tasklist.shared.AdditionalInformationTaskListSectionService.FIELD_LOOKUP_PURPOSE;
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
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionController;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformation;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationController;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class AdditionalInformationTaskListSectionServiceTest {

  public static final String ADDITIONAL_INFORMATION_SECTION = "Additional information";
  
  public static final int ADDITIONAL_INFORMATION_DISPLAY_ORDER = 30;

  public static final String EIA_DIRECTION_TASK_LIST_ITEM = "EIA screening direction";

  public static final String SUPPORTING_INFORMATION_TASK_LIST_ITEM = "Supporting information";

  private AdditionalInformationTaskListSectionService additionalInformationTaskListSectionService;

  @Mock
  private SupportingInformationService supportingInformationService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private EiaDirectionService eiaDirectionService;

  private ApplicationVersion applicationVersion;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    additionalInformationTaskListSectionService = new AdditionalInformationTaskListSectionService(supportingInformationService,
        applicationAssetService, fieldService, eiaDirectionService);
  }

  @Test
  void getSection_additionalInformationSection() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);
    when(eiaDirectionService.findEiaDirection(applicationVersion)).thenReturn(Optional.of(new EiaDirection()));
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.of(new SupportingInformation()));

    Optional<TaskListSection> taskListSectionOptional = additionalInformationTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, ADDITIONAL_INFORMATION_SECTION, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getSection_withTaskListItemsNotStarted() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);
    when(eiaDirectionService.findEiaDirection(applicationVersion)).thenReturn(Optional.empty());
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.empty());

    Optional<TaskListSection> taskListSectionOptional = additionalInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        EIA_DIRECTION_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(EiaDirectionController.class).getEiaDirectionForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        SUPPORTING_INFORMATION_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withTaskListItemsCompleted() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);
    when(eiaDirectionService.findEiaDirection(applicationVersion)).thenReturn(Optional.of(new EiaDirection()));
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.of(new SupportingInformation()));

    Optional<TaskListSection> taskListSectionOptional = additionalInformationTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        EIA_DIRECTION_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(EiaDirectionController.class).getEiaDirectionForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        SUPPORTING_INFORMATION_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withNoEiaItem_Onshore() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset2.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field2Json);
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
  void getSection_withNoEiaItem_UnknownShore() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset3);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset3.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field3Json);
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
  void getSection_withNoEiaItem_Terminal() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.terminalAsset1);
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
}