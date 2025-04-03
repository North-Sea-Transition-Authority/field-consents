package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = AdditionalAssetsController.class)
class AdditionalAssetsControllerTest extends AbstractApplicationControllerTest {
  
  @MockitoBean
  private AssetService assetService;

  @MockitoBean
  private AssetSummaryService assetSummaryService;

  @MockitoBean
  private ApplicationAssetService applicationAssetService;

  @MockitoBean
  private ApplicationFlagService applicationFlagService;

  @MockitoBean
  private AdditionalAssetsFormValidator additionalAssetsFormValidator;

  @MockitoBean
  private AdditionalAssetsService additionalAssetsService;

  @MockitoBean
  private AdditionalAssetSelectionFormValidator additionalAssetSelectionFormValidator;

  private ApplicationVersion applicationVersion;

  private String expectBaseAdditionalAssetsUrl;

  private static final String TASK_LIST_URL = "/applications/" + ApplicationTestUtil.APPLICATION_ID + "/task-list";

  private static final String ADDITIONAL_ASSETS_REQUIRED_VIEW = "fcs/assets/additionalAssetsRequired";

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    expectBaseAdditionalAssetsUrl = ApplicationAssetTestUtil.BASE_ASSETS_URL;

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void addAdditionalAsset_ValidUser() throws Exception {
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).addAdditionalAsset(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/additionalAsset"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_ADD)
        .containsEntry(AdditionalAssetsController.CANCEL_URL_ATTR_NAME, expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AssetSelectionForm) model.get("form"))
        .extracting(AssetSelectionForm::assetKey)
        .isNull();
  }

  @SecurityTest
  void addAdditionalAsset_unauthorizedUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).addAdditionalAsset(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveNewAsset_emptyForm() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("assetKey", "required", "Select an asset");
      return null;
    })
        .when(additionalAssetSelectionFormValidator).validate(any(), any(), eq(applicationVersion));

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveNewAsset(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf())
            .param("assetKey", ""))
    .andExpect(status().isOk())
    .andExpect(view().name("fcs/assets/additionalAsset"))
    .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_ADD)
        .containsEntry(AdditionalAssetsController.CANCEL_URL_ATTR_NAME, expectBaseAdditionalAssetsUrl + "/summary");
    assertThat((AssetSelectionForm) model.get("form"))
        .extracting(AssetSelectionForm::assetKey)
        .isNull();
  }

  @Test
  void saveNewAsset_validForm() throws Exception {
    var assetJson = field1Json;

    var form = AssetSelectionForm.from(assetJson.getAssetKey());

    when(assetService.getAsset(form.getAssetKey().orElseThrow())).thenReturn(assetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(field1JsonWithOperatorAndLicences);

    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveNewAsset(
            ApplicationTestUtil.APPLICATION_ID, form, null)))
            .with(user(user))
            .with(csrf())
            .param("assetKey", assetJson.getAssetKey().toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/summary"));

    verify(additionalAssetsService, times(1))
        .saveAdditionalAsset(applicationVersion, field1JsonWithOperatorAndLicences);
  }

  @Test
  void saveNewAsset_terminalAsset_passThroughValidation() throws Exception {
    var assetJson = terminal1Json;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());

    when(assetService.getAsset(assetJson.getAssetKey())).thenReturn(assetJson);

    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveNewAsset(
        ApplicationTestUtil.APPLICATION_ID, form, null)))
            .with(user(user))
            .with(csrf())
            .param("assetKey", assetJson.getAssetKey().toString()))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void viewAdditionalAssetsSummary_noAssets() throws Exception {
    when(applicationAssetService.secondaryAssetsExist(applicationVersion)).thenReturn(Boolean.FALSE);

    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));
  }

  @Test
  void viewAdditionalAssetsSummary_withAssets() throws Exception {
    when(applicationAssetService.secondaryAssetsExist(applicationVersion)).thenReturn(Boolean.TRUE);
    when(assetSummaryService.getSummaryViews(applicationVersion)).thenReturn(ApplicationAssetTestUtil.assetViews);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
        )
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

  @SecurityTest
  void viewAdditionalAssetsSummary_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isForbidden());
  }

  @Test
  void saveAssetsSummary_invalidForm() throws Exception {

    doCallRealMethod().when(additionalAssetsFormValidator).validate(any(), any());
    when(assetSummaryService.getSummaryViews(applicationVersion)).thenReturn(ApplicationAssetTestUtil.assetViews);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
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
  void saveAssetsSummary_validFormWithAssetsToAdd() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
        ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherAssetsToAdd", Boolean.TRUE.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/new"));
  }

  @Test
  void saveAssetsSummary_validFormWithNoMoreAssetsToAdd() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
            ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherAssetsToAdd", Boolean.FALSE.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));
  }

  @SecurityTest
  void saveAssetsSummary_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(
        ApplicationTestUtil.APPLICATION_ID,null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteAssetConfirm_assetExists() throws Exception {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(assetSummaryService.getSummaryView(ApplicationAssetTestUtil.fieldAsset2))
        .thenReturn(ApplicationAssetTestUtil.assetView2);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/deleteAsset"))
        .andReturn().getModelAndView();

    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_DELETE)
        .containsEntry("submitUrl", expectBaseAdditionalAssetsUrl + "/" +  ApplicationAssetTestUtil.fieldAsset2.getAssetNo() + "/delete")
        .containsEntry(AdditionalAssetsController.CANCEL_URL_ATTR_NAME, expectBaseAdditionalAssetsUrl + "/summary")
        .containsEntry("assetView", ApplicationAssetTestUtil.assetView2);
  }

  @Test
  void deleteAssetConfirm_noAssetExists() {

    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenThrow(new EntityNotFoundException("Asset with application version id 1 and asset no 2 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
            .with(user(user))
        )
    )
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Asset with application version id %s and asset no %s not found"
            .formatted(applicationVersion.getId(), ApplicationAssetTestUtil.fieldAsset2.getAssetNo()));
  }

  @SecurityTest
  void deleteAssetConfirm_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAssetConfirm(
                ApplicationTestUtil.APPLICATION_ID, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void deleteAsset_assetExists() throws Exception {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);

    mockMvc.perform(
            post(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/additional-assets/required"));

    verify(additionalAssetsService, times(1))
        .deleteAdditionalAsset(ApplicationAssetTestUtil.fieldAsset2);
  }

  @Test
  void deleteAsset_noAssetExists() {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenThrow(new EntityNotFoundException("Asset with application_version_id 1 and asset_no 2 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
            ApplicationTestUtil.APPLICATION_ID, null,  ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
            .with(user(user))
        ))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Asset with application_version_id %s and asset_no %s not found"
            .formatted(applicationVersion.getId(),  ApplicationAssetTestUtil.fieldAsset2.getAssetNo()));
  }

  @Test
  void deleteAsset_otherAssetsExist() throws Exception {
    when(applicationAssetService.getSecondaryAsset(applicationVersion, ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(applicationAssetService.secondaryAssetsExist(applicationVersion)).thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null, ApplicationAssetTestUtil.fieldAsset2.getAssetNo())))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/summary"));

    verify(additionalAssetsService, times(1))
        .deleteAdditionalAsset(ApplicationAssetTestUtil.fieldAsset2);
  }

  @SecurityTest
  void deleteAsset_noUser() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(
                ApplicationTestUtil.APPLICATION_ID, null,  ApplicationAssetTestUtil.fieldAsset2.getAssetNo()))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getAdditionalAssetsRequiredForm() throws Exception {
    var expectedAdditionalAssetsSetupForm = new AdditionalAssetsSetupForm();
    when(applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion))
        .thenReturn(expectedAdditionalAssetsSetupForm);

    var modelAndView =
        mockMvc.perform(
            get(ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
            )
            .andExpect(status().isOk())
            .andExpect(view().name(ADDITIONAL_ASSETS_REQUIRED_VIEW))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.CANCEL_URL_ATTR_NAME, TASK_LIST_URL);
    assertThat((AdditionalAssetsSetupForm) model.get("form"))
        .isEqualTo(expectedAdditionalAssetsSetupForm);
  }

  @SecurityTest
  void getAdditionalAssetsRequiredForm_noUser() throws Exception {
    when(applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion))
        .thenReturn(new AdditionalAssetsSetupForm());

    mockMvc.perform(
        get(ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(
            ApplicationTestUtil.APPLICATION_ID
        )))
    ).andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveAdditionalAssetsRequiredForm_emptyForm() throws Exception {
    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
                .saveAdditionalAssetsRequiredForm(
                    ApplicationTestUtil.APPLICATION_ID,
                    null,
                    null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(ADDITIONAL_ASSETS_REQUIRED_VIEW))
        .andReturn().getModelAndView();

    verifyNoInteractions(applicationFlagService);

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(AdditionalAssetsController.CANCEL_URL_ATTR_NAME, TASK_LIST_URL);
    assertThat((AdditionalAssetsSetupForm) model.get("form"))
        .usingRecursiveComparison()
        .isEqualTo(new AdditionalAssetsSetupForm());
  }

  @Test
  void saveAdditionalAssetsRequiredForm_otherAssetsRequired() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(user(user))
            .with(csrf())
            .param("otherAssetsRequired", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseAdditionalAssetsUrl + "/new"));

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS, true);
  }

  @Test
  void saveAdditionalAssetsRequiredForm_otherAssetsNotRequired() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(user(user))
            .with(csrf())
            .param("otherAssetsRequired", "false"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/" + ApplicationTestUtil.APPLICATION_ID + "/task-list"));

    verify(applicationFlagService, times(1))
        .addOrUpdateApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS, false);
  }

  @SecurityTest
  void saveAdditionalAssetsRequiredForm_unauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(AdditionalAssetsController.class)
            .saveAdditionalAssetsRequiredForm(
                ApplicationTestUtil.APPLICATION_ID,
                null,
                null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}
