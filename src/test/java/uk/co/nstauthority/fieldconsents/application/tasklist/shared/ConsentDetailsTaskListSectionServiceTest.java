package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.secondaryAssets;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.terminalAsset1;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.ADDITIONAL_ASSETS_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_DETAILS_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_LENGTH_TASK_LIST_ITEM;
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
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsController;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ConsentDetailsTaskListSectionServiceTest {

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationFlagService applicationFlagService;

  private ConsentDetailsTaskListSectionService consentDetailsTaskListSectionService;

  private ApplicationVersion applicationVersion;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    consentDetailsTaskListSectionService = new ConsentDetailsTaskListSectionService(
        consentLengthService,
        applicationAssetService,
        applicationFlagService);
  }

  @Test
  void getSection_consentDetailsTaskListSection() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    assertTaskListSection(taskListSection, CONSENT_DETAILS_SECTION, CONSENT_DETAILS_DISPLAY_ORDER);
  }

  @Test
  void getSection_consentDetailsTaskListItemsNotCompleted() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(
        Collections.emptyList());
    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        CONSENT_LENGTH_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        ADDITIONAL_ASSETS_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_consentDetailsTaskListItemCompleted_withFieldPrimaryAssetAndFlareApplication() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(secondaryAssets);

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        CONSENT_LENGTH_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        ADDITIONAL_ASSETS_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_consentDetailsTaskListItemCompleted_withFieldPrimaryAssetAndVentApplication() {
    ApplicationVersion ventAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(ventAppVersion);
    when(consentLengthService.findConsentLengthDetails(ventAppVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationAssetService.getPrimaryAsset(ventAppVersion)).thenReturn(fieldAsset1);
    when(applicationAssetService.getSecondaryAssets(ventAppVersion)).thenReturn(secondaryAssets);

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(ventAppVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(0),
        CONSENT_LENGTH_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(ventAppVersion.getApplication().getId()))
    );

    assertTaskListItem(
        taskListItems.get(1),
        ADDITIONAL_ASSETS_TASK_LIST_ITEM,
        TaskListLabel.NOT_STARTED,
        ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ventAppVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_consentDetailsTaskListItemCompleted_withFieldPrimaryAssetAndProduction() {
    ApplicationVersion productionAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(productionAppVersion);
    when(consentLengthService.findConsentLengthDetails(productionAppVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationAssetService.getPrimaryAsset(productionAppVersion)).thenReturn(fieldAsset1);

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(productionAppVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        CONSENT_LENGTH_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(productionAppVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_consentDetailsTaskListItemCompleted_withTerminalPrimaryAsset() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(terminalAsset1);

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);
    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        CONSENT_LENGTH_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_noAdditionalAssets_sectionCompleted() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(
        Collections.emptyList());
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(Boolean.FALSE));

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);

    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(1),
        ADDITIONAL_ASSETS_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withAdditionalAssetsAndEmptyList_sectionInProgress() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(
        Collections.emptyList());
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(Boolean.TRUE));

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);

    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(1),
        ADDITIONAL_ASSETS_TASK_LIST_ITEM,
        TaskListLabel.IN_PROGRESS,
        ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationVersion.getApplication().getId()))
    );
  }

  @Test
  void getSection_withAdditionalAssetsNonEmptyList_sectionCompleted() {
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(secondaryAssets);
    when(applicationFlagService.findFlagValue(
        applicationVersion,
        ApplicationFlagType.HAS_SECONDARY_ASSETS
    )).thenReturn(Optional.of(Boolean.TRUE));

    Optional<TaskListSection> taskListSectionOptional = consentDetailsTaskListSectionService.getSection(applicationVersion);

    TaskListSection taskListSection = taskListSectionOptional.orElseThrow(RuntimeException::new);
    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(2);

    assertTaskListItem(
        taskListItems.get(1),
        ADDITIONAL_ASSETS_TASK_LIST_ITEM,
        TaskListLabel.COMPLETED,
        ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationVersion.getApplication().getId()))
    );
  }
}