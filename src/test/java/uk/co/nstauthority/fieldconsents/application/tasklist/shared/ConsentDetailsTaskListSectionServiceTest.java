package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.HAS_SECONDARY_ASSETS;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.WILL_GAS_BE_INJECTED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsController;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleEmissionsFormValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.flare.ApplicationRationaleFlareController;
import uk.co.nstauthority.fieldconsents.application.rationale.production.ApplicationRationaleProductionController;
import uk.co.nstauthority.fieldconsents.application.rationale.production.ApplicationRationaleProductionFormValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.vent.ApplicationRationaleVentController;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionController;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ConsentDetailsTaskListSectionServiceTest {

  private static final String APPLICATION_RATIONALE_TASK_LIST_ITEM_NAME = "Application rationale";
  private static final String ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME = "Additional fields and licences";
  private static final String GAS_INJECTION_TASK_LIST_ITEM_NAME = "Gas injection";

  private static final TaskListItem TASK_LIST_ITEM = new TaskListItem("Example", TaskListLabel.COMPLETED, "#");

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationFlagService applicationFlagService;

  @Mock
  private ApplicationRationaleService applicationRationaleService;

  @Mock
  private ApplicationRationaleProductionFormValidator applicationRationaleProductionFormValidator;

  @Mock
  private ApplicationRationaleEmissionsFormValidator applicationRationaleEmissionsFormValidator;

  @Spy
  @InjectMocks
  private ConsentDetailsTaskListSectionService taskListSectionService;

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSection_empty(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    doReturn(Optional.empty()).when(taskListSectionService).getApplicationRationaleTaskListItem(applicationVersion);
    doReturn(Optional.empty()).when(taskListSectionService).getConsentDurationTaskListItem(applicationVersion);
    doReturn(Optional.empty()).when(taskListSectionService).getAdditionalAssetsTaskListItem(applicationVersion);
    doReturn(Optional.empty()).when(taskListSectionService).getGasInjectionTaskListItem(applicationVersion);

    assertThat(taskListSectionService.getSection(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSection_notEmpty(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    doReturn(Optional.of(TASK_LIST_ITEM)).when(taskListSectionService).getApplicationRationaleTaskListItem(
        applicationVersion);
    doReturn(Optional.of(TASK_LIST_ITEM)).when(taskListSectionService).getConsentDurationTaskListItem(
        applicationVersion);
    doReturn(Optional.of(TASK_LIST_ITEM)).when(taskListSectionService).getAdditionalAssetsTaskListItem(
        applicationVersion);
    doReturn(Optional.of(TASK_LIST_ITEM)).when(taskListSectionService).getGasInjectionTaskListItem(applicationVersion);

    assertThat(taskListSectionService.getSection(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListSection(
            "Application details",
            10,
            List.of(TASK_LIST_ITEM, TASK_LIST_ITEM, TASK_LIST_ITEM, TASK_LIST_ITEM)
        ));
  }

  @Test
  void getApplicationRationaleTaskListItem_flareApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var applicationId = applicationVersion.getApplication().getId();

    doReturn(TaskListLabel.COMPLETED)
        .when(taskListSectionService)
        .getApplicationRationaleTaskListLabel(applicationVersion);

    assertThat(taskListSectionService.getApplicationRationaleTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            APPLICATION_RATIONALE_TASK_LIST_ITEM_NAME,
            TaskListLabel.COMPLETED,
            ReverseRouter.route(on(ApplicationRationaleFlareController.class).getForm(applicationId))
        ));
  }

  @Test
  void getApplicationRationaleTaskListItem_ventApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var applicationId = applicationVersion.getApplication().getId();

    doReturn(TaskListLabel.COMPLETED)
        .when(taskListSectionService)
        .getApplicationRationaleTaskListLabel(applicationVersion);

    assertThat(taskListSectionService.getApplicationRationaleTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            APPLICATION_RATIONALE_TASK_LIST_ITEM_NAME,
            TaskListLabel.COMPLETED,
            ReverseRouter.route(on(ApplicationRationaleVentController.class).getForm(applicationId))
        ));
  }

  @Test
  void getApplicationRationaleTaskListItem_productionApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationId = applicationVersion.getApplication().getId();

    doReturn(TaskListLabel.COMPLETED)
        .when(taskListSectionService)
        .getApplicationRationaleTaskListLabel(applicationVersion);

    assertThat(taskListSectionService.getApplicationRationaleTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            APPLICATION_RATIONALE_TASK_LIST_ITEM_NAME,
            TaskListLabel.COMPLETED,
            ReverseRouter.route(on(ApplicationRationaleProductionController.class).getForm(applicationId))
        ));
  }

  @Test
  void getApplicationRationaleTaskListLabel_applicationRationaleNotFoundAndNoLocationOrHostLocationAssetsFound() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of());
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.empty());

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.NOT_STARTED);
  }

  @Test
  void getApplicationRationaleTaskListLabel_applicationRationaleNotFoundAndLocationAssetFound() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of(field1Json));
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.empty());

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.IN_PROGRESS);
  }

  @Test
  void getApplicationRationaleTaskListLabel_applicationRationaleNotFoundAndHostLocationAssetFound() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of());
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(field1Json));

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.IN_PROGRESS);
  }

  @Test
  void getApplicationRationaleTaskListLabel_productionApplication_formInvalid() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationRationale = new ApplicationRationale();
    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of());
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(field1Json));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));
      return invocation;
    }).when(applicationRationaleProductionFormValidator).validate(any(), any());

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.IN_PROGRESS);
  }

  @Test
  void getApplicationRationaleTaskListLabel_productionApplication_formValid() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationRationale = new ApplicationRationale();
    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of());
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(field1Json));

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.COMPLETED);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"}, mode = EnumSource.Mode.INCLUDE)
  void getApplicationRationaleTaskListLabel_emissionApplication_formInvalid(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationRationale = new ApplicationRationale();
    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of());
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(field1Json));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));
      return invocation;
    }).when(applicationRationaleEmissionsFormValidator).validate(any(), any());

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.IN_PROGRESS);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"}, mode = EnumSource.Mode.INCLUDE)
  void getApplicationRationaleTaskListLabel_emissionApplication_formValid(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationRationale = new ApplicationRationale();
    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(List.of());
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(field1Json));

    assertThat(taskListSectionService.getApplicationRationaleTaskListLabel(applicationVersion))
        .isEqualTo(TaskListLabel.COMPLETED);
  }


  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getConsentDurationTaskListItem_notFound(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var applicationId = applicationVersion.getApplication().getId();

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.empty());

    assertThat(taskListSectionService.getConsentDurationTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            "Consent duration",
            TaskListLabel.NOT_STARTED,
            ReverseRouter.route(
                on(ConsentLengthController.class).getConsentLengthForm(applicationId))
        ));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getConsentDurationTaskListItem(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var applicationId = applicationVersion.getApplication().getId();

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(new ConsentLengthDetails()));

    assertThat(taskListSectionService.getConsentDurationTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            "Consent duration",
            TaskListLabel.COMPLETED,
            ReverseRouter.route(
                on(ConsentLengthController.class).getConsentLengthForm(applicationId))
        ));
  }

  @Test
  void getAdditionalAssetsTaskListItem_production() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    assertThat(taskListSectionService.getAdditionalAssetsTaskListItem(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getAdditionalAssetsTaskListItem_nonProduction_nonField(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetId(1);
    applicationAsset.setAssetType(AssetType.TERMINAL);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(applicationAsset);

    assertThat(taskListSectionService.getAdditionalAssetsTaskListItem(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getAdditionalAssetsTaskListItem_noSecondaryAsset_noFlag(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var applicationId = applicationVersion.getApplication().getId();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(1);
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(Collections.emptyList());
    when(applicationFlagService.findFlagValue(applicationVersion, HAS_SECONDARY_ASSETS)).thenReturn(Optional.empty());

    assertThat(taskListSectionService.getAdditionalAssetsTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME,
            TaskListLabel.NOT_STARTED,
            ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationId))
        ));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getAdditionalAssetsTaskListItem_noSecondaryAsset_falseFlag(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var applicationId = applicationVersion.getApplication().getId();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(1);
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(Collections.emptyList());
    when(applicationFlagService.findFlagValue(applicationVersion, HAS_SECONDARY_ASSETS)).thenReturn(Optional.of(false));

    assertThat(taskListSectionService.getAdditionalAssetsTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME,
            TaskListLabel.COMPLETED,
            ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationId))
        ));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getAdditionalAssetsTaskListItem_noSecondaryAsset_trueFlag(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var applicationId = applicationVersion.getApplication().getId();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(1);
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(Collections.emptyList());
    when(applicationFlagService.findFlagValue(applicationVersion, HAS_SECONDARY_ASSETS)).thenReturn(Optional.of(true));

    assertThat(taskListSectionService.getAdditionalAssetsTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME,
            TaskListLabel.IN_PROGRESS,
            ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationId))
        ));
  }

  @ParameterizedTest
  @MethodSource("getAdditionalAssetsTaskListItemParams")
  void getAdditionalAssetsTaskListItem(
      ApplicationVersion applicationVersion,
      ApplicationAsset primaryAsset,
      List<ApplicationAsset> secondaryAssets,
      Boolean hasSecondaryAssetsFlag,
      TaskListItem taskListItem
  ) {
    lenient().when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    lenient().when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(secondaryAssets);
    lenient().when(applicationFlagService.findFlagValue(applicationVersion, HAS_SECONDARY_ASSETS)).thenReturn(Optional.ofNullable(hasSecondaryAssetsFlag));

    assertThat(taskListSectionService.getAdditionalAssetsTaskListItem(applicationVersion)).isEqualTo(Optional.ofNullable(taskListItem));
  }

  private static Stream<Arguments> getAdditionalAssetsTaskListItemParams() {
    var fieldAsset = new ApplicationAsset();
    fieldAsset.setAssetId(1);
    fieldAsset.setAssetType(AssetType.FIELD);

    var terminalAsset = new ApplicationAsset();
    terminalAsset.setAssetId(1);
    terminalAsset.setAssetType(AssetType.TERMINAL);

    return Stream.of(
        Arguments.of(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION),
            terminalAsset,
            Collections.<ApplicationAsset>emptyList(),
            null,
            null // unsupported for flare production applications
        ),
        Arguments.of(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE),
            terminalAsset,
            Collections.<ApplicationAsset>emptyList(),
            null,
            null // terminals assets don't need this section
        ),
        Arguments.of(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE),
            fieldAsset,
            Collections.<ApplicationAsset>emptyList(),
            null,
            new TaskListItem(
                ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME,
                TaskListLabel.NOT_STARTED,
                ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(APPLICATION_ID))
            )
        ),
        Arguments.of(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE),
            fieldAsset,
            Collections.singletonList(new ApplicationAsset()),
            null,
            new TaskListItem(
                ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME,
                TaskListLabel.NOT_STARTED,
                ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(APPLICATION_ID))
            )
        ),
        Arguments.of(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE),
            fieldAsset,
            Collections.<ApplicationAsset>emptyList(),
            true,
            new TaskListItem(
                ADDITIONAL_FIELDS_LICENSES_TASK_LIST_ITEM_NAME,
                TaskListLabel.IN_PROGRESS,
                ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(APPLICATION_ID))
            )
        )
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getGasInjectionTaskListItem_nonProduction(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    assertThat(taskListSectionService.getGasInjectionTaskListItem(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getGasInjectionTaskListItem_flagPresent(Boolean flag) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationId = applicationVersion.getApplication().getId();

    when(applicationFlagService.findFlagValue(applicationVersion, WILL_GAS_BE_INJECTED)).thenReturn(Optional.of(flag));

    assertThat(taskListSectionService.getGasInjectionTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            GAS_INJECTION_TASK_LIST_ITEM_NAME,
            TaskListLabel.COMPLETED,
            ReverseRouter.route(on(GasInjectionController.class).getGasInjectionForm(applicationId))
        ));
  }

  @Test
  void getGasInjectionTaskListItem_flagNotPresent() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationId = applicationVersion.getApplication().getId();

    when(applicationFlagService.findFlagValue(applicationVersion, WILL_GAS_BE_INJECTED)).thenReturn(Optional.empty());

    assertThat(taskListSectionService.getGasInjectionTaskListItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new TaskListItem(
            GAS_INJECTION_TASK_LIST_ITEM_NAME,
            TaskListLabel.NOT_STARTED,
            ReverseRouter.route(on(GasInjectionController.class).getGasInjectionForm(applicationId))
        ));
  }

}
