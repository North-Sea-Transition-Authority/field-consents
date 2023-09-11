package uk.co.nstauthority.fieldconsents.application.rationale.vent;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationRationaleVentController.class)
class ApplicationRationaleVentControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationRationaleVentController> CONTROLLER_CLASS = ApplicationRationaleVentController.class;
  private static final String VIEW_NAME = "fcs/application/application-rationale/vent-form";

  @MockBean
  private ApplicationRationaleVentService applicationRationaleVentService;

  @MockBean
  private ApplicationRationaleService applicationRationaleService;

  @MockBean
  private ApplicationAssetService applicationAssetService;

  @MockBean
  private ApplicationRationaleVentFormValidator applicationRationaleVentFormValidator;

  @MockBean
  private AssetService assetService;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  private List<AssetJson> ventingLocations;

  private AssetJson hostLocation;

  private ApplicationAsset primaryApplicationAsset;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    applicationRationale = new ApplicationRationale();

    ventingLocations = new ArrayList<>();
    ventingLocations.add(terminal1Json);

    hostLocation = terminal1Json;

    primaryApplicationAsset = new ApplicationAsset();

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
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(ventingLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));

    primaryApplicationAsset.setFieldId(1);
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
        .containsEntry("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .containsEntry("ventingLocations", ventingLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("ventingLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleVentForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleVentForm.empty());
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsTerminal() throws Exception {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(ventingLocations);
    when(applicationRationaleService.getHostLocation(applicationVersion)).thenReturn(Optional.of(hostLocation));

    primaryApplicationAsset.setTerminalId(1);
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
        .containsEntry("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .containsEntry("ventingLocations", ventingLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("ventingLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleVentForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleVentForm.empty());
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

    primaryApplicationAsset.setTerminalId(1);
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
        .containsEntry("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .containsEntry("ventingLocations", ventingLocations.stream().map(ApplicationAssetView::from).toList())
        .containsEntry("hostLocation", RestSearchItem.from(hostLocation))
        .containsEntry("ventingLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    var expectedForm = new ApplicationRationaleVentForm(rationaleType, null, null, null, null);
    expectedForm.increaseComment().setInputValue(comment);

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleVentForm.class))
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
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, EDIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .saveForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void saveForm() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "comment";
    var ventingAssetKeys = List.of("assetKey1", "assetKey2", "assetKey3");
    var hostAssetKey = "assetKey1";

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("increaseComment.inputValue", comment)
            .param("ventingLocationAssetKeys", String.join(",", ventingAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    var expectedForm = new ApplicationRationaleVentForm(
        rationaleType,
        null,
        null,
        ventingAssetKeys,
        hostAssetKey
    );
    expectedForm.increaseComment().setInputValue(comment);

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(applicationRationaleVentFormValidator).validate(
        argThat(o -> {
          var form = (ApplicationRationaleVentForm) o;
          return Objects.equals(expectedForm.increaseComment().getInputValue(), form.increaseComment().getInputValue())
              && Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.ventingLocationAssetKeys().containsAll(form.ventingLocationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey());
        }),
        any(BindingResult.class)
    );

    verify(applicationRationaleVentService).saveApplicationRationale(
        applicationVersion,
        ApplicationRationaleType.INCREASE,
        comment,
        ventingAssetKeys,
        hostAssetKey
    );
  }

  @Test
  void saveForm_validationFailed() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "comment";
    var ventingAssetKeys = List.of("123FIELD");
    var hostAssetKey = ventingAssetKeys.get(0);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("rationaleType", "errorCode", "message");
      return null;
    })
        .when(applicationRationaleVentFormValidator)
        .validate(any(ApplicationRationaleVentForm.class), any(BindingResult.class));

    when(assetService.getAsset(eq(AssetKey.from(hostAssetKey)), anyString())).thenReturn(Optional.empty());
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("increaseComment.inputValue", comment)
            .param("ventingLocationAssetKeys", String.join(",", ventingAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(applicationRationaleVentService, never()).saveApplicationRationale(any(), any(), any(), any(), any());
  }
}
