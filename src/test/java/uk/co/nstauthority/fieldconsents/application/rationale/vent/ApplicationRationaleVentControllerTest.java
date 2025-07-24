package uk.co.nstauthority.fieldconsents.application.rationale.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field2AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleEmissionService;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleEmissionsForm;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleEmissionsFormValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.EmissionDailyAverage;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationRationaleVentController.class)
class ApplicationRationaleVentControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationRationaleVentController> CONTROLLER_CLASS = ApplicationRationaleVentController.class;
  private static final String VIEW_NAME = "fcs/application/application-rationale/vent-form";

  @MockitoBean
  private ApplicationRationaleVentService applicationRationaleVentService;

  @MockitoBean
  private ApplicationAssetService applicationAssetService;

  @MockitoBean
  private ApplicationRationaleEmissionsFormValidator validator;

  @MockitoBean
  private ApplicationRationaleService applicationRationaleService;

  @MockitoBean
  private AssetService assetService;

  @MockitoBean
  private ApplicationRationaleEmissionService applicationRationaleEmissionService;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  private List<AssetJson> ventingLocations;

  private AssetJson hostLocation;

  private ApplicationAsset primaryApplicationAsset;

  private EmissionDailyAverage emissionDailyAverage;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    applicationRationale = new ApplicationRationale();

    ventingLocations = new ArrayList<>();
    ventingLocations.add(terminal1Json);

    hostLocation = terminal1Json;

    primaryApplicationAsset = new ApplicationAsset();

    emissionDailyAverage = new EmissionDailyAverage(
        applicationVersion.getApplication().getType(),
        LocalDate.now().getYear(),
        BigDecimal.valueOf(10),
        FlareVentUnit.G_PER_MOL
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
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsField() throws Exception {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(ventingLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));
    when(applicationRationaleEmissionService.findEmissionDailyAverage(applicationVersion)).thenReturn(Optional.of(emissionDailyAverage));

    primaryApplicationAsset.setAssetId(1);
    primaryApplicationAsset.setAssetType(AssetType.FIELD);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null));
    when(applicationRationaleService.getAssetSearchUrl(primaryApplicationAsset)).thenReturn(assetSearchRestUrl);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("increaseRadio", ApplicationRationaleType.INCREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .containsEntry("ventingLocations", ventingLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("ventingLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null)))
        .containsEntry("emissionDailyAverage", emissionDailyAverage);

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleEmissionsForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleEmissionsForm.empty());
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsTerminal() throws Exception {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(ventingLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));

    primaryApplicationAsset.setAssetId(1);
    primaryApplicationAsset.setAssetType(AssetType.TERMINAL);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null));
    when(applicationRationaleService.getAssetSearchUrl(primaryApplicationAsset)).thenReturn(assetSearchRestUrl);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("increaseRadio", ApplicationRationaleType.INCREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .containsEntry("ventingLocations", ventingLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("ventingLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleEmissionsForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleEmissionsForm.empty());
  }

  @Test
  void getForm_applicationRationaleExists() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "Some comments";

    applicationRationale.setId(1);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);
    applicationRationale.setComment(comment);

    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(ventingLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));

    primaryApplicationAsset.setAssetId(1);
    primaryApplicationAsset.setAssetType(AssetType.TERMINAL);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null));
    when(applicationRationaleService.getAssetSearchUrl(primaryApplicationAsset)).thenReturn(assetSearchRestUrl);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("increaseRadio", ApplicationRationaleType.INCREASE)
        .containsEntry("decreaseRadio", ApplicationRationaleType.DECREASE)
        .containsEntry("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .containsEntry("ventingLocations", ventingLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("ventingLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null)));

    var expectedForm = new ApplicationRationaleEmissionsForm(
        rationaleType,
        null,
        null,
        null,
        ventingLocations.stream().map(AssetJson::getAssetKey).map(AssetKey::toString).toList(),
        hostLocation.getAssetKey().toString()
    );
    expectedForm.increaseComment().setInputValue(comment);

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleEmissionsForm.class))
        .usingRecursiveComparison()
        .isEqualTo(expectedForm);
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
    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void saveForm_increase() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "comment";
    var assetKeys = List.of(field1AssetJson.getAssetKey(), field2AssetJson.getAssetKey(), terminal1AssetJson.getAssetKey());
    var assetKeyStrings = assetKeys.stream().map(AssetKey::toString).toList();
    var hostAssetKey = assetKeys.getFirst();

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("increaseComment.inputValue", comment)
            .param("locationAssetKeys", String.join(",", assetKeyStrings))
            .param("hostLocationAssetKey", hostAssetKey.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null))));

    var expectedForm = new ApplicationRationaleEmissionsForm(
        rationaleType,
        null,
        null,
        null,
        assetKeyStrings,
        hostAssetKey.toString()
    );
    expectedForm.increaseComment().setInputValue(comment);

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(validator).validate(
        argThat(form ->
            Objects.equals(expectedForm.increaseComment().getInputValue(), form.increaseComment().getInputValue())
              && Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.locationAssetKeys().containsAll(form.locationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey())
        ),
        any(BindingResult.class)
    );

    verify(applicationRationaleVentService).saveApplicationRationale(
        applicationVersion,
        ApplicationRationaleType.INCREASE,
        comment,
        assetKeys,
        hostAssetKey
    );
  }

  @Test
  void saveForm_decrease() throws Exception {
    var rationaleType = ApplicationRationaleType.DECREASE;
    var comment = "comment";
    var assetKeys = List.of(field1AssetJson.getAssetKey(), field2AssetJson.getAssetKey(), terminal1AssetJson.getAssetKey());
    var assetKeyStrings = assetKeys.stream().map(AssetKey::toString).toList();
    var hostAssetKey = assetKeys.getFirst();

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("decreaseComment.inputValue", comment)
            .param("locationAssetKeys", String.join(",", assetKeyStrings))
            .param("hostLocationAssetKey", hostAssetKey.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID, null))));

    var expectedForm = new ApplicationRationaleEmissionsForm(
        rationaleType,
        null,
        null,
        null,
        assetKeyStrings,
        hostAssetKey.toString()
    );
    expectedForm.decreaseComment().setInputValue(comment);

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(validator).validate(
        argThat(form ->
            Objects.equals(expectedForm.increaseComment().getInputValue(), form.increaseComment().getInputValue())
              && Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.locationAssetKeys().containsAll(form.locationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey())
        ),
        any(BindingResult.class)
    );

    verify(applicationRationaleVentService).saveApplicationRationale(
        applicationVersion,
        ApplicationRationaleType.DECREASE,
        comment,
        assetKeys,
        hostAssetKey
    );
  }

  @Test
  void saveForm_validationFailed() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "comment";
    var assetKeys = List.of(field1AssetJson.getAssetKey(), field2AssetJson.getAssetKey(), terminal1AssetJson.getAssetKey());
    var assetKeyStrings = assetKeys.stream().map(AssetKey::toString).toList();
    var hostAssetKey = assetKeys.getFirst();

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("rationaleType", "errorCode", "message");
      return null;
    })
        .when(validator)
        .validate(any(ApplicationRationaleEmissionsForm.class), any(BindingResult.class));

    when(assetService.findAsset(hostAssetKey)).thenReturn(Optional.empty());
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var assetSearchRestUrl = ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null));
    when(applicationRationaleService.getAssetSearchUrl(primaryApplicationAsset)).thenReturn(assetSearchRestUrl);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("increaseComment.inputValue", comment)
            .param("locationAssetKeys", String.join(",", assetKeyStrings))
            .param("hostLocationAssetKey", hostAssetKey.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(applicationRationaleVentService, never()).saveApplicationRationale(any(), any(), any(), any(), any());
  }
}
