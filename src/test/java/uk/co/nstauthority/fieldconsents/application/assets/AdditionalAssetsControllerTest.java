package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.assets.AdditionalAssetsSetupForm;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = AdditionalAssetsController.class)
class AdditionalAssetsControllerTest extends AbstractControllerTest {

  private static final String ASSET_KEY = "1FIELD";
  
  @MockBean
  private AssetService assetService;

  @MockBean
  private FieldService fieldService;

  @MockBean
  private AssetSummaryService assetSummaryService;

  @MockBean
  private ApplicationAssetService applicationAssetService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ApplicationFlagService applicationFlagService;

  @MockBean
  private AdditionalAssetsFormValidator additionalAssetsFormValidator;

  @MockBean
  private AdditionalAssetsService additionalAssetsService;

  @MockBean
  private AdditionalAssetSelectionFormValidator additionalAssetSelectionFormValidator;

  private ApplicationVersion applicationVersion;

  private String expectBaseAdditionalAssetsUrl;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    expectBaseAdditionalAssetsUrl = ApplicationAssetTestUtil.BASE_ASSETS_URL;

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  void addAdditionalAsset_ValidUser() throws Exception {
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).addAdditionalAsset(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/additionalAsset"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_ADD)
        .containsEntry("cancelUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AssetSelectionForm) model.get("form"))
        .extracting(AssetSelectionForm::getAssetKey)
        .isNull();
  }

  @Test
  void addAdditionalAsset_unauthorizedUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).addAdditionalAsset(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveNewAsset_emptyForm() throws Exception {

    doCallRealMethod().when(additionalAssetSelectionFormValidator).validate(any(), any());

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveNewAsset(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
        .with(csrf())
        .param("assetKey", ""))
    .andExpect(status().isOk())
    .andExpect(view().name("fcs/assets/additionalAsset"))
    .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_ADD)
        .containsEntry("cancelUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AssetSelectionForm) model.get("form"))
        .extracting(AssetSelectionForm::getAssetKey)
        .isNull();
  }

  @Test
  @WithMockUser
  void saveNewAsset_validForm() throws Exception {
    AssetSelectionForm form = new AssetSelectionForm(ASSET_KEY);
    AssetJson assetJson = field1Json;

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(assetService.getAsset(form.getAssetKey())).thenReturn(assetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(field1JsonWithOperatorAndLicences);

    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveNewAsset(
            ApplicationTestUtil.APPLICATION_ID, form, null)))
            .with(csrf())
            .param("assetKey", ASSET_KEY))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/summary"));

    verify(additionalAssetsService, times(1))
        .saveAdditionalAsset(applicationVersion, field1JsonWithOperatorAndLicences);
  }

  @Test
  @WithMockUser
  void saveNewAsset_terminalAsset_passThroughValidation() throws Exception {
    AssetJson assetJson = terminal1Json;
    AssetSelectionForm form = new AssetSelectionForm(assetJson.getSelectionId());

    when(assetService.getAsset(form.getAssetKey())).thenReturn(assetJson);

    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveNewAsset(
            ApplicationTestUtil.APPLICATION_ID, form, null)))
            .with(csrf())
            .param("assetKey", assetJson.getSelectionId()))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser
  void viewAdditionalAssetsSummary_noAssets() throws Exception {
    when(applicationAssetService.secondaryAssetsExist(applicationVersion)).thenReturn(Boolean.FALSE);

    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  @WithMockUser
  void viewAdditionalAssetsSummary_withAssets() throws Exception {
    when(applicationAssetService.secondaryAssetsExist(applicationVersion)).thenReturn(Boolean.TRUE);
    when(assetSummaryService.getSummaryViews(applicationVersion)).thenReturn(ApplicationAssetTestUtil.assetViews);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/assetsSummaryForm"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_SUMMARY)
        .containsEntry("assetViews", ApplicationAssetTestUtil.assetViews)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AdditionalAssetsForm) model.get("form"))
        .extracting(AdditionalAssetsForm::getHasOtherAssetsToAdd)
        .isNull();
  }

  @Test
  void viewAdditionalAssetsSummary_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void saveAssetsSummary_invalidForm() throws Exception {

    doCallRealMethod().when(additionalAssetsFormValidator).validate(any(), any());
    when(assetSummaryService.getSummaryViews(applicationVersion)).thenReturn(ApplicationAssetTestUtil.assetViews);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/assetsSummaryForm"))
        .andReturn().getModelAndView();

   var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_SUMMARY)
        .containsEntry("assetViews", ApplicationAssetTestUtil.assetViews)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AdditionalAssetsForm) model.get("form"))
        .extracting(AdditionalAssetsForm::getHasOtherAssetsToAdd)
        .isNull();
  }

  @Test
  @WithMockUser
  void saveAssetsSummary_validFormWithAssetsToAdd() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherAssetsToAdd", Boolean.TRUE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/new"));
  }

  @Test
  @WithMockUser
  void saveAssetsSummary_validFormWithNoMoreAssetsToAdd() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherAssetsToAdd", Boolean.FALSE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  void saveAssetsSummary_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
        ApplicationTestUtil.APPLICATION_ID,null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void deleteAssetConfirm_assetExists() throws Exception {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(assetSummaryService.getSummaryView(ApplicationAssetTestUtil.fieldAsset2))
        .thenReturn(ApplicationAssetTestUtil.assetView2);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/deleteAsset"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_DELETE)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/" +  ApplicationAssetTestUtil.fieldAsset2.getAssetNo() + "/delete")
        .containsEntry("cancelUrl", expectBaseAdditionalAssetsUrl + "/summary")
        .containsEntry("assetView", ApplicationAssetTestUtil.assetView2);
  }

  @Test
  @WithMockUser
  void deleteAssetConfirm_noAssetExists() {

    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenThrow(new EntityNotFoundException("Asset with application version id 1 and asset no 2 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
        )
    )
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Asset with application version id %s and asset no %s not found"
            .formatted(applicationVersion.getId(), ApplicationAssetTestUtil.fieldAsset2.getAssetNo()));
  }

  @Test
  void deleteAssetConfirm_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void deleteAsset_assetExists() throws Exception {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);

    mockMvc.perform(
            post(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/additional-assets/required"));

    verify(additionalAssetsService, times(1))
        .deleteAdditionalAsset(ApplicationAssetTestUtil.fieldAsset2);
  }

  @Test
  @WithMockUser
  void deleteAsset_noAssetExists() {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenThrow(new EntityNotFoundException("Asset with application_version_id 1 and asset_no 2 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
            ApplicationTestUtil.APPLICATION_ID, null,  ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Asset with application_version_id %s and asset_no %s not found"
            .formatted(applicationVersion.getId(),  ApplicationAssetTestUtil.fieldAsset2.getAssetNo()));
  }

  @Test
  @WithMockUser
  void deleteAsset_otherAssetsExist() throws Exception {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(applicationAssetService.secondaryAssetsExist(applicationVersion)).thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/summary"));

    verify(additionalAssetsService, times(1))
        .deleteAdditionalAsset(ApplicationAssetTestUtil.fieldAsset2);
  }

  @Test
  void deleteAsset_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null,  ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void getAdditionalAssetsRequiredForm() throws Exception {
    when(applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion))
        .thenReturn(new AdditionalAssetsSetupForm());

    mockMvc.perform(
        get(ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(
            ApplicationTestUtil.APPLICATION_ID
        )))
    ).andExpect(status().isOk());
  }

  @Test
  void getAdditionalAssetsRequiredForm_noUser() throws Exception {
    when(applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion))
        .thenReturn(new AdditionalAssetsSetupForm());

    mockMvc.perform(
        get(ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(
            ApplicationTestUtil.APPLICATION_ID
        )))
    ).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveAdditionalAssetsRequiredForm_emptyForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/additionalAssetsRequired"));

    verifyNoInteractions(applicationFlagService);
  }

  @Test
  @WithMockUser
  void saveAdditionalAssetsRequiredForm_otherAssetsRequired() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(csrf())
            .param("otherAssetsRequired", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/new"));

    verify(applicationFlagService, times(1))
        .deleteApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS);
    verify(applicationFlagService, times(1))
        .saveApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS, true);
  }

  @Test
  @WithMockUser
  void saveAdditionalAssetsRequiredForm_otherAssetsNotRequired() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(csrf())
            .param("otherAssetsRequired", "false"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/" + ApplicationTestUtil.APPLICATION_ID + "/task-list/"));

    verify(applicationFlagService, times(1))
        .deleteApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS);
    verify(applicationFlagService, times(1))
        .saveApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS, false);
  }

  @Test
  void saveAdditionalAssetsRequiredForm_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}