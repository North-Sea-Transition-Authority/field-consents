package uk.co.nstauthority.fieldconsents.application.rationale.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ContextConfiguration(classes = ApplicationRationaleProductionController.class)
class ApplicationRationaleProductionControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationRationaleProductionController> CONTROLLER_CLASS = ApplicationRationaleProductionController.class;
  private static final String VIEW_NAME = "fcs/application/application-rationale/production-form";

  @MockBean
  private ApplicationRationaleProductionService applicationRationaleProductionService;

  @MockBean
  private ApplicationAssetService applicationAssetService;

  @MockBean
  private ApplicationRationaleProductionFormValidator applicationRationaleProductionFormValidator;

  @MockBean
  private ApplicationRationaleService applicationRationaleService;

  @MockBean
  private AssetService assetService;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  private List<AssetJson> productionLocations;

  private AssetJson hostLocation;

  private ApplicationAsset primaryApplicationAsset;

  private OilAndGasMaximums oilAndGasMaximums;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationRationale = new ApplicationRationale();

    productionLocations = new ArrayList<>();
    productionLocations.add(terminal1Json);

    hostLocation = terminal1Json;

    primaryApplicationAsset = new ApplicationAsset();

    oilAndGasMaximums = new OilAndGasMaximums(
        LocalDate.now().getYear(),
        BigDecimal.valueOf(10),
        ProductionUnit.KSCM_PER_DAY,
        BigDecimal.valueOf(20),
        ProductionUnit.KSCM_PER_DAY
    );

    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsField() throws Exception {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(productionLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));
    when(applicationRationaleProductionService.findOilAndGasMaximums(applicationVersion)).thenReturn(Optional.of(oilAndGasMaximums));

    primaryApplicationAsset.setAssetId(1);
    primaryApplicationAsset.setAssetType(AssetType.FIELD);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchAllAssets(null)).replace("?term", "");
    assertThat(assetSearchRestUrl).doesNotContain("?term=");

    assertThat(model)
        .containsEntry("increaseRadio", ApplicationRationaleType.INCREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("productionLocations", productionLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("productionLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)))
        .containsEntry("oilAndGasMaximums", oilAndGasMaximums);

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleProductionForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleProductionForm.empty());
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsTerminal() throws Exception {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(productionLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));

    primaryApplicationAsset.setAssetId(1);
    primaryApplicationAsset.setAssetType(AssetType.TERMINAL);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null)).replace("?term", "");
    assertThat(assetSearchRestUrl).doesNotContain("?term=");

    assertThat(model)
        .containsEntry("increaseRadio", ApplicationRationaleType.INCREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("extensionRadio", ApplicationRationaleType.EXTENSION)
        .containsEntry("otherRadio", ApplicationRationaleType.OTHER)
        .containsEntry("productionLocations", productionLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("productionLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleProductionForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleProductionForm.empty());
  }

  @ParameterizedTest
  @CsvSource({
      "EXTENSION, comment,",
      "OTHER,, comment",
  })
  void getForm_extension_other_applicationRationaleExists(
      ApplicationRationaleType rationaleType, 
      String extensionComment, 
      String otherComment
  ) throws Exception {
    var comment = "comment";

    applicationRationale.setId(1);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);
    applicationRationale.setComment(comment);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(productionLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));

    primaryApplicationAsset.setAssetId(1);
    primaryApplicationAsset.setAssetType(AssetType.TERMINAL);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null)).replace("?term", "");
    assertThat(assetSearchRestUrl).doesNotContain("?term=");

    assertThat(model)
        .containsEntry("increaseRadio", ApplicationRationaleType.INCREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("extensionRadio", ApplicationRationaleType.EXTENSION)
        .containsEntry("otherRadio", ApplicationRationaleType.OTHER)
        .containsEntry("productionLocations", productionLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("productionLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleProductionForm.class))
        .extracting(
            ApplicationRationaleProductionForm::rationaleType,
            form -> form.extensionComment().getInputValue(),
            form -> form.otherComment().getInputValue()
        ).containsExactly(
            rationaleType,
            extensionComment,
            otherComment
        );
  }

  @SecurityTest
  void saveForm_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveForm_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = {"INCREASE", "DECREASE"}, mode = Mode.INCLUDE)
  void saveForm_increase_decrease(ApplicationRationaleType rationaleType) throws Exception {
    var flaringAssetKeys = List.of("assetKey1", "assetKey2", "assetKey3");
    var hostAssetKey = "assetKey1";

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .param("rationaleType", rationaleType.toString())
            .param("productionLocationAssetKeys", String.join(",", flaringAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    var expectedForm = new ApplicationRationaleProductionForm(
        rationaleType,
        null,
        null,
        null,
        flaringAssetKeys,
        hostAssetKey
    );

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(applicationRationaleProductionFormValidator).validate(
        argThat(o -> {
          var form = (ApplicationRationaleProductionForm) o;
          return Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.productionLocationAssetKeys().containsAll(form.productionLocationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey());
        }),
        any(BindingResult.class)
    );

    verify(applicationRationaleProductionService).saveApplicationRationale(
        applicationVersion,
        rationaleType,
        null,
        null,
        flaringAssetKeys,
        hostAssetKey
    );
  }

  @Test
  void saveForm_extension() throws Exception {
    var rationaleType = ApplicationRationaleType.EXTENSION;
    var productionLocationAssetKeys = List.of("assetKey1", "assetKey2", "assetKey3");
    var hostAssetKey = "assetKey1";
    var comment = "comment";

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .param("rationaleType", rationaleType.toString())
            .param("extensionComment.inputValue", comment)
            .param("productionLocationAssetKeys", String.join(",", productionLocationAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    var expectedForm = new ApplicationRationaleProductionForm(
        rationaleType,
        null,
        null,
        null,
        productionLocationAssetKeys,
        hostAssetKey
    );
    expectedForm.extensionComment().setInputValue(comment);

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(applicationRationaleProductionFormValidator).validate(
        argThat(o -> {
          var form = (ApplicationRationaleProductionForm) o;
          return Objects.equals(expectedForm.extensionComment().getInputValue(), form.extensionComment().getInputValue())
              && Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.productionLocationAssetKeys().containsAll(form.productionLocationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey());
        }),
        any(BindingResult.class)
    );

    verify(applicationRationaleProductionService).saveApplicationRationale(
        applicationVersion,
        rationaleType,
        comment,
        null,
        productionLocationAssetKeys,
        hostAssetKey
    );
  }

  @Test
  void saveForm_other() throws Exception {
    var rationaleType = ApplicationRationaleType.OTHER;
    var productionLocationAssetKeys = List.of("assetKey1", "assetKey2", "assetKey3");
    var hostAssetKey = "assetKey1";
    var comment = "comment";

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .param("rationaleType", rationaleType.toString())
            .param("otherComment.inputValue", comment)
            .param("productionLocationAssetKeys", String.join(",", productionLocationAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    var expectedForm = new ApplicationRationaleProductionForm(
        rationaleType,
        null,
        null,
        null,
        productionLocationAssetKeys,
        hostAssetKey
    );
    expectedForm.otherComment().setInputValue(comment);

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(applicationRationaleProductionFormValidator).validate(
        argThat(o -> {
          var form = (ApplicationRationaleProductionForm) o;
          return Objects.equals(expectedForm.otherComment().getInputValue(), form.otherComment().getInputValue())
              && Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.productionLocationAssetKeys().containsAll(form.productionLocationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey());
        }),
        any(BindingResult.class)
    );

    verify(applicationRationaleProductionService).saveApplicationRationale(
        applicationVersion,
        rationaleType,
        null,
        comment,
        productionLocationAssetKeys,
        hostAssetKey
    );
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void saveForm_validationFailed(ApplicationRationaleType rationaleType) throws Exception {
    var comment = "comment";
    var productionAssetKeys = List.of("123FIELD");
    var hostAssetKey = productionAssetKeys.get(0);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("rationaleType", "errorCode", "message");
      return null;
    })
        .when(applicationRationaleProductionFormValidator)
        .validate(any(ApplicationRationaleProductionForm.class), any(BindingResult.class));

    when(assetService.getAsset(eq(AssetKey.from(hostAssetKey)), anyString())).thenReturn(Optional.empty());
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .param("rationaleType", rationaleType.toString())
            .param("otherComment.inputValue", comment)
            .param("productionLocationAssetKeys", String.join(",", productionAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(applicationRationaleProductionService, never()).saveApplicationRationale(any(), any(), any(), any(), any(), any());
  }
}
