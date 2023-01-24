package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;

import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationAssetController.class)
class ApplicationAssetControllerTest extends AbstractControllerTest {

  private static final String ASSET_KEY = "1FIELD";
  
  @MockBean
  private AssetService assetService;

  @MockBean
  private AssetSummaryService assetSummaryService;

  @MockBean
  private ApplicationAssetService applicationAssetService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private AdditionalAssetsFormValidator additionalAssetsFormValidator;
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
            get(ReverseRouter.route(on(ApplicationAssetController.class).addAdditionalAsset(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/additionalAsset"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(ApplicationAssetController.PAGE_TITLE_ATTR_NAME, ApplicationAssetController.PAGE_NAME_ADD)
        .containsEntry("cancelUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AssetSelectionForm) model.get("form"))
        .extracting(AssetSelectionForm::getAssetKey)
        .isNull();
  }

  @Test
  void addAdditionalAsset_unauthorizedUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationAssetController.class).addAdditionalAsset(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveNewAsset_emptyForm() throws Exception {
    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).saveNewAsset(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
        .with(csrf()))
    .andExpect(status().isOk())
    .andExpect(view().name("fcs/assets/additionalAsset"))
    .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(ApplicationAssetController.PAGE_TITLE_ATTR_NAME, ApplicationAssetController.PAGE_NAME_ADD)
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

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).saveNewAsset(
            ApplicationTestUtil.APPLICATION_ID, form, null)))
            .with(csrf())
        .param("assetKey", ASSET_KEY))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/summary"));

    when(assetService.getAsset(form.getAssetKey())).thenReturn(assetJson);
    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<AssetJson> assetJsonCaptor = ArgumentCaptor.forClass(AssetJson.class);
    verify(applicationAssetService, times(1))
        .saveAdditionalAsset(applicationVersionArgumentCaptor.capture(), assetJsonCaptor.capture());
  }

  @Test
  @WithMockUser
  void viewAdditionalAssetsSummary_noAssets() throws Exception {
    when(applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)).thenReturn(Boolean.FALSE);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  @WithMockUser
  void viewAdditionalAssetsSummary_withAssets() throws Exception {
    when(applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)).thenReturn(Boolean.TRUE);
    when(assetSummaryService.getSummaryViews(applicationVersion)).thenReturn(ApplicationAssetTestUtil.assetViews);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/assetsSummaryForm"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(ApplicationAssetController.PAGE_TITLE_ATTR_NAME, ApplicationAssetController.PAGE_NAME_SUMMARY)
        .containsEntry("assetViews", ApplicationAssetTestUtil.assetViews)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AdditionalAssetsForm) model.get("form"))
        .extracting(AdditionalAssetsForm::getHasOtherAssetsToAdd)
        .isNull();
  }

  @Test
  void viewAdditionalAssetsSummary_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void saveAssetsSummary_invalidForm() throws Exception {

    doCallRealMethod().when(additionalAssetsFormValidator).validate(any(), any());
    when(assetSummaryService.getSummaryViews(applicationVersion)).thenReturn(ApplicationAssetTestUtil.assetViews);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/assetsSummaryForm"))
        .andReturn().getModelAndView();

   var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(ApplicationAssetController.PAGE_TITLE_ATTR_NAME, ApplicationAssetController.PAGE_NAME_SUMMARY)
        .containsEntry("assetViews", ApplicationAssetTestUtil.assetViews)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AdditionalAssetsForm) model.get("form"))
        .extracting(AdditionalAssetsForm::getHasOtherAssetsToAdd)
        .isNull();
  }

  @Test
  @WithMockUser
  void saveAssetsSummary_validFormWithAssetsToAdd() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherAssetsToAdd", Boolean.TRUE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/new"));
  }

  @Test
  @WithMockUser
  void saveAssetsSummary_validFormWithNoMoreAssetsToAdd() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherAssetsToAdd", Boolean.FALSE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  void saveAssetsSummary_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationAssetController.class).saveAssetsSummary(
        ApplicationTestUtil.APPLICATION_ID,null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void deleteAssetConfirm_assetExists() throws Exception {
    when(applicationAssetService.getAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset1);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationAssetController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/deleteAsset"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(ApplicationAssetController.PAGE_TITLE_ATTR_NAME, ApplicationAssetController.PAGE_NAME_DELETE)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/" +  ApplicationAssetTestUtil.fieldAsset1.getAssetNo() + "/delete")
        .containsEntry("cancelUrl", expectBaseAdditionalAssetsUrl + "/summary");
    assertThat(model.get("assetView").getClass()).isEqualTo(AssetView.class);
    assertThat((AssetView) model.get("assetView"))
        .extracting(AssetView::displayOrder,
            AssetView::assetNo,
            AssetView::assetName,
            AssetView::assetOperatorName,
            AssetView::deleteUrl)
        .containsExactly(ApplicationAssetTestUtil.assetView.displayOrder(),
            ApplicationAssetTestUtil.assetView.assetNo(),
            ApplicationAssetTestUtil.assetView.assetName(),
            ApplicationAssetTestUtil.assetView.assetOperatorName(),
            ApplicationAssetTestUtil.assetView.deleteUrl()
        );
  }

  @Test
  @WithMockUser
  void deleteAssetConfirm_noAssetExists() {

    when(applicationAssetService.getAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))
        .thenThrow(new EntityNotFoundException("Asset with application version id 1 and asset no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(ApplicationAssetController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset1.getAssetNo())))
        )
    )
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Asset with application version id %s and asset no %s not found"
            .formatted(applicationVersion.getId(), ApplicationAssetTestUtil.fieldAsset1.getAssetNo()));
  }

  @Test
  void deleteAssetConfirm_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationAssetController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void deleteAsset_assetExists() throws Exception {
    when(applicationAssetService.getAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset1);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationAssetController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null, ApplicationAssetTestUtil.fieldAsset1.getAssetNo())))
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    verify(applicationAssetService, times(1)).deleteAsset(ApplicationAssetTestUtil.fieldAsset1);
  }

  @Test
  @WithMockUser
  void deleteAsset_noAssetExists() {
    when(applicationAssetService.getAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))
        .thenThrow(new EntityNotFoundException("Asset with application_version_id 1 and asset_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(ApplicationAssetController.class).deleteAsset(
            ApplicationTestUtil.APPLICATION_ID, null,  ApplicationAssetTestUtil.fieldAsset1.getAssetNo())))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Asset with application_version_id %s and asset_no %s not found"
            .formatted(applicationVersion.getId(),  ApplicationAssetTestUtil.fieldAsset1.getAssetNo()));
  }

  @Test
  @WithMockUser
  void deleteAsset_otherAssetsExist() throws Exception {
    when(applicationAssetService.getAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)).thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationAssetController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null, ApplicationAssetTestUtil.fieldAsset1.getAssetNo())))
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/summary"));

    verify(applicationAssetService, times(1)).deleteAsset(ApplicationAssetTestUtil.fieldAsset1);
  }

  @Test
  void deleteAsset_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationAssetController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null,  ApplicationAssetTestUtil.fieldAsset1.getAssetNo()))))
        .andExpect(status().isUnauthorized());
  }
}