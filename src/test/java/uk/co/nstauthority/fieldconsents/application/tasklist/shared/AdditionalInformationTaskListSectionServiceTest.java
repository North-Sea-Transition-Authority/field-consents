package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.tasklist.shared.AdditionalInformationTaskListSectionService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeController;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationController;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;

@ExtendWith(MockitoExtension.class)
class AdditionalInformationTaskListSectionServiceTest {

  public static final String ADDITIONAL_INFORMATION_SECTION = "Additional information";

  public static final int ADDITIONAL_INFORMATION_DISPLAY_ORDER = 30;

  public static final String EIA_DIRECTION_TASK_LIST_ITEM = "EIA screening direction";

  public static final String SUPPORTING_INFORMATION_TASK_LIST_ITEM = "Supporting information";

  @Mock
  private SupportingInformationService supportingInformationService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private EiaDirectionService eiaDirectionService;

  @InjectMocks
  private AdditionalInformationTaskListSectionService additionalInformationTaskListSectionService;

  private ApplicationVersion productionApplicationVersion;

  @BeforeEach
  void setUp() {
    productionApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @ParameterizedTest
  @MethodSource("getSectionParams")
  void getSection_production_nonEia(ApplicationAsset asset, FieldJson fieldJson, List<TaskListItem> expectedTaskListItems) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(asset);
    when(fieldService.getField(asset.getAssetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(fieldJson);
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.empty());

    var taskListSection = additionalInformationTaskListSectionService.getSection(applicationVersion).orElseThrow();
    var taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(expectedTaskListItems.size());

    for (var i = 0; i < expectedTaskListItems.size(); i++) {
      assertTaskListItem(
          taskListItems.get(i),
          expectedTaskListItems.get(i).displayName(),
          expectedTaskListItems.get(i).label(),
          expectedTaskListItems.get(i).actionUrl()
      );
    }
  }

  private static Stream<Arguments> getSectionParams() {
    return Stream.of(
        Arguments.of(
            ApplicationAssetTestUtil.fieldAsset2, // onshore
            FieldTestUtil.field2Json,
            Collections.singletonList(
                new TaskListItem(
                    SUPPORTING_INFORMATION_TASK_LIST_ITEM,
                    TaskListLabel.NOT_STARTED,
                    ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(APPLICATION_ID))
                )
            )
        ),
        Arguments.of(
            ApplicationAssetTestUtil.fieldAsset3, // unknown shore?
            FieldTestUtil.field3Json,
            Collections.singletonList(
                new TaskListItem(
                    SUPPORTING_INFORMATION_TASK_LIST_ITEM,
                    TaskListLabel.NOT_STARTED,
                    ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(APPLICATION_ID))
                )
            )
        )
    );
  }

  @ParameterizedTest
  @CsvSource({
      "false, false, NOT_STARTED",
      "false, true, IN_PROGRESS",
      "true, false, COMPLETED", // completed takes precedence over started
      "true, true, COMPLETED"
  })
  void getSection_production_eia(boolean isCompleted, boolean isStarted, String expectedLabel) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getAssetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(FieldTestUtil.field1Json);
    when(supportingInformationService.findSupportingInformation(applicationVersion)).thenReturn(Optional.empty());

    lenient().when(eiaDirectionService.isEiaDirectionStarted(applicationVersion)).thenReturn(isStarted);
    lenient().when(eiaDirectionService.isEiaDirectionCompleted(applicationVersion)).thenReturn(isCompleted);

    var taskListSection = additionalInformationTaskListSectionService.getSection(applicationVersion).orElseThrow();
    var taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);
    var eiaTaskListItem = taskListItems.get(0);

    assertTaskListItem(
        eiaTaskListItem,
        EIA_DIRECTION_TASK_LIST_ITEM,
        TaskListLabel.valueOf(expectedLabel),
        ReverseRouter.route(on(ProjectPurposeController.class).getForm(APPLICATION_ID))
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSection_nonProductionApplication_noEiaTaskListItem(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset2);

    var taskListSection = additionalInformationTaskListSectionService.getSection(applicationVersion).orElseThrow();
    var taskListItems = taskListSection.items();

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
    when(applicationAssetService.getPrimaryAsset(productionApplicationVersion)).thenReturn(ApplicationAssetTestUtil.terminalAsset1);
    when(supportingInformationService.findSupportingInformation(productionApplicationVersion)).thenReturn(Optional.empty());

    var taskListSection = additionalInformationTaskListSectionService.getSection(productionApplicationVersion).orElseThrow();
    var taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        SUPPORTING_INFORMATION_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(productionApplicationVersion.getApplication().getId()))
    );
  }
}
