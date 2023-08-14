package uk.co.nstauthority.fieldconsents.application.rationale.flare;

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
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.ArrayList;
import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleFormService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationRationaleFlareController.class)
class ApplicationRationaleFlareControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationRationaleFlareController> CONTROLLER_CLASS = ApplicationRationaleFlareController.class;
  private static final String VIEW_NAME = "fcs/application/application-rationale/flare-form";

  @MockBean
  private ApplicationRationaleFlareService applicationRationaleFlareService;

  @MockBean
  private ApplicationRationaleFormService applicationRationaleFormService;

  @MockBean
  private ApplicationAssetService applicationAssetService;

  @MockBean
  private ApplicationRationaleFlareFormValidator applicationRationaleFlareFormValidator;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  private List<ApplicationAssetView> flaringLocations;

  private RestSearchItem hostLocationRestSearchItem;

  private ApplicationAsset primaryApplicationAsset;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    applicationRationale = new ApplicationRationale();

    flaringLocations = new ArrayList<>();
    flaringLocations.add(new ApplicationAssetView("test1", "test 1", true));

    hostLocationRestSearchItem = RestSearchItem.from(terminal1Json);

    primaryApplicationAsset = new ApplicationAsset();

    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsField() throws Exception {
    when(applicationRationaleFlareService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleFlareService.getFlaringLocations(applicationVersion)).thenReturn(flaringLocations);
    when(applicationRationaleFlareService.getHostLocation(applicationVersion)).thenReturn(hostLocationRestSearchItem);

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
        .containsEntry("flaringLocations", flaringLocations)
        .containsEntry("hostLocation", hostLocationRestSearchItem)
        .containsEntry("flaringLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleFlareForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleFlareForm.empty());
  }

  @Test
  void getForm_applicationRationaleDoesNotExist_primaryAssetIsTerminal() throws Exception {
    when(applicationRationaleFlareService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    when(applicationRationaleFlareService.getFlaringLocations(applicationVersion)).thenReturn(flaringLocations);
    when(applicationRationaleFlareService.getHostLocation(applicationVersion)).thenReturn(hostLocationRestSearchItem);

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
        .containsEntry("flaringLocations", flaringLocations)
        .containsEntry("hostLocation", hostLocationRestSearchItem)
        .containsEntry("flaringLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleFlareForm.class))
        .usingRecursiveComparison()
        .isEqualTo(ApplicationRationaleFlareForm.empty());
  }

  @Test
  void getForm_applicationRationaleExists() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "Some comments";

    applicationRationale.setId(1);
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setRationaleType(rationaleType);
    applicationRationale.setComment(comment);

    when(applicationRationaleFlareService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    when(applicationRationaleFlareService.getFlaringLocations(applicationVersion)).thenReturn(flaringLocations);
    when(applicationRationaleFlareService.getHostLocation(applicationVersion)).thenReturn(hostLocationRestSearchItem);

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
        .containsEntry("flaringLocations", flaringLocations)
        .containsEntry("hostLocation", hostLocationRestSearchItem)
        .containsEntry("flaringLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("hostLocationSearchUrl", assetSearchRestUrl)
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    var expectedForm = new ApplicationRationaleFlareForm(rationaleType, null, null, null, null);
    expectedForm.increaseComment().setInputValue(comment);

    assertThat(model)
        .containsKey("form")
        .extracting(m -> m.get("form"))
        .asInstanceOf(type(ApplicationRationaleFlareForm.class))
        .usingRecursiveComparison()
        .isEqualTo(expectedForm);
  }

  @Test
  void saveForm() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "comment";
    var flaringAssetKeys = List.of("assetKey1", "assetKey2", "assetKey3");
    var hostAssetKey = "assetKey1";

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("increaseComment.inputValue", comment)
            .param("flaringLocationAssetKeys", String.join(",", flaringAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    var expectedForm = new ApplicationRationaleFlareForm(
        rationaleType,
        null,
        null,
        flaringAssetKeys,
        hostAssetKey
    );
    expectedForm.increaseComment().setInputValue(comment);

    // We can't use `eq()` because the StringInput in the form is a different object
    verify(applicationRationaleFlareFormValidator).validate(
        argThat(o -> {
          var form = (ApplicationRationaleFlareForm) o;
          return Objects.equals(expectedForm.increaseComment().getInputValue(), form.increaseComment().getInputValue())
              && Objects.equals(expectedForm.rationaleType(), form.rationaleType())
              && expectedForm.flaringLocationAssetKeys().containsAll(form.flaringLocationAssetKeys())
              && Objects.equals(expectedForm.hostLocationAssetKey(), form.hostLocationAssetKey());
        }),
        any(BindingResult.class)
    );

    verify(applicationRationaleFlareService).saveApplicationRationale(
        applicationVersion,
        ApplicationRationaleType.INCREASE,
        comment,
        flaringAssetKeys,
        hostAssetKey
    );
  }

  @Test
  void saveForm_validationFailed() throws Exception {
    var rationaleType = ApplicationRationaleType.INCREASE;
    var comment = "comment";
    var flaringAssetKeys = List.of("assetKey1");
    var hostAssetKey = "assetKey1";

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("rationaleType", "errorCode", "message");
      return null;
    })
        .when(applicationRationaleFlareFormValidator)
        .validate(any(ApplicationRationaleFlareForm.class), any(BindingResult.class));

    when(applicationRationaleFormService.getFlaringLocationsFromForm(any())).thenReturn(Collections.emptyList());
    when(applicationRationaleFormService.getHostLocationFormForm(any())).thenReturn(RestSearchItem.EMPTY_REST_SEARCH_ITEM);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getForm(APPLICATION_ID)))
            .param("rationaleType", rationaleType.toString())
            .param("increaseComment.inputValue", comment)
            .param("flaringLocationAssetKeys", String.join(",", flaringAssetKeys))
            .param("hostLocationAssetKey", hostAssetKey)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(applicationRationaleFlareService, never()).saveApplicationRationale(any(), any(), any(), any(), any());
  }
}
