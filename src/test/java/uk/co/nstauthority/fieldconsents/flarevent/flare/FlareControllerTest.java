package uk.co.nstauthority.fieldconsents.flarevent.flare;

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

import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareController.class)
class FlareControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private FlareService flareService;

  @MockBean
  private FlareSummaryService flareSummaryService;

  @MockBean
  private FlareFormValidator flareFormValidator;

  @MockBean
  private FlareSetupFormValidator flareSetupFormValidator;

  private ApplicationVersion applicationVersion;

  private String expectBaseFlaresUrl;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareTestUtil.flareAppVersion;
    expectBaseFlaresUrl = FlareTestUtil.BASE_FLARES_URL;
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersion);
    when(applicationService.getApplicationById(applicationVersion.getApplication().getId())).thenReturn(applicationVersion.getApplication());
  }

  @Test
  @WithMockUser
  void addFlare_validUser() throws Exception {
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).addFlare(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/editFlareForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_ADD)
        .containsEntry("flareTypes", FlareTestUtil.flareTypesAsMap)
        .containsEntry("cancelUrl", expectBaseFlaresUrl);
    assertThat((FlareForm) model.get("form"))
        .extracting(FlareForm::getFlareType,
            flareForm -> flareForm.getDescription().getInputValue(),
            FlareForm::getMeteredFlag,
            flareForm -> flareForm.getCommentsMeteredYes().getInputValue(),
            flareForm -> flareForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(null, null, null, null, null);
  }

  @Test
  void addFlare_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareController.class).addFlare(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveNewFlare_invalidForm() throws Exception {

    doCallRealMethod().when(flareFormValidator).validate(any(), any());

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveNewFlare(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/editFlareForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_ADD)
        .containsEntry("flareTypes", FlareTestUtil.flareTypesAsMap)
        .containsEntry("cancelUrl", expectBaseFlaresUrl);
    assertThat((FlareForm) model.get("form"))
        .extracting(FlareForm::getFlareType,
            flareForm -> flareForm.getDescription().getInputValue(),
            FlareForm::getMeteredFlag,
            flareForm -> flareForm.getCommentsMeteredYes().getInputValue(),
            flareForm -> flareForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(null, null, null, null, null);
  }

  @Test
  @WithMockUser
  void saveNewFlare_validForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveNewFlare(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseFlaresUrl));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareForm> flareFormArgumentCaptor = ArgumentCaptor.forClass(FlareForm.class);
    verify(flareService, times(1))
        .saveNewFlare(applicationVersionArgumentCaptor.capture(), flareFormArgumentCaptor.capture());
  }

  @Test
  void saveNewFlare_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveNewFlare(
        ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void viewFlaresSummary_noFlares() throws Exception {
    when(flareService.flaresExistForApplicationVersion(applicationVersion)).thenReturn(Boolean.FALSE);
    mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).viewFlaresSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  @WithMockUser
  void viewFlaresSummary_flaresExist() throws Exception {
    when(flareService.flaresExistForApplicationVersion(applicationVersion)).thenReturn(Boolean.TRUE);
    when(flareSummaryService.getSummaryViews(applicationVersion)).thenReturn(FlareTestUtil.flareViews);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(FlareController.class).viewFlaresSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/flaresSummaryForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_SUMMARY)
        .containsEntry("flareViews", FlareTestUtil.flareViews)
        .containsEntry("submitUrl", expectBaseFlaresUrl);
    assertThat((FlareSetupForm) model.get("form"))
        .extracting(FlareSetupForm::getHasOtherFlaresToAdd)
        .isNull();
  }

  @Test
  void viewFlaresSummary_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).viewFlaresSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void saveFlaresSummary_invalidForm() throws Exception {

    doCallRealMethod().when(flareSetupFormValidator).validate(any(), any());
    when(flareSummaryService.getSummaryViews(applicationVersion)).thenReturn(FlareTestUtil.flareViews);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlaresSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/flaresSummaryForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_SUMMARY)
        .containsEntry("flareViews", FlareTestUtil.flareViews)
        .containsEntry("submitUrl", expectBaseFlaresUrl);
    assertThat((FlareSetupForm) model.get("form"))
        .extracting(FlareSetupForm::getHasOtherFlaresToAdd)
        .isNull();
  }

  @Test
  @WithMockUser
  void saveFlaresSummary_validFormMoreFlares() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlaresSummary(
        ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherFlaresToAdd", Boolean.TRUE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseFlaresUrl + "/new"));
  }

  @Test
  @WithMockUser
  void saveFlaresSummary_validFormNoMoreFlares() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlaresSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .param("hasOtherFlaresToAdd", Boolean.FALSE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  void saveFlaresSummary_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlaresSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void editFlare_flareFound() throws Exception {

    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenReturn(FlareTestUtil.flareHp);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).editFlare(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/editFlareForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_EDIT)
        .containsEntry("flareTypes", FlareTestUtil.flareTypesAsMap)
        .containsEntry("cancelUrl", expectBaseFlaresUrl);
    assertThat((FlareForm) model.get("form"))
        .extracting(FlareForm::getFlareType,
            flareForm -> flareForm.getDescription().getInputValue(),
            FlareForm::getMeteredFlag,
            flareForm -> flareForm.getCommentsMeteredYes().getInputValue(),
            flareForm -> flareForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(FlareTestUtil.flareHp.getFlareType(),
            FlareTestUtil.flareHp.getDescription(),
            FlareTestUtil.flareHp.getMeteredFlag(),
            FlareTestUtil.flareHp.getComments(),
            null);
  }

  @Test
  @WithMockUser
  void editFlare_noFlareFound() {

    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenThrow(new EntityNotFoundException("Flare with application_version_id 1 and flare_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(FlareController.class).editFlare(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp)))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Flare with application_version_id %s and flare_no %s not found"
            .formatted(applicationVersion.getId(), FlareTestUtil.flareNoHp));
  }

  @Test
  void editFlare_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).editFlare(applicationVersion.getApplication().getId(),
                FlareTestUtil.flareNoHp))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveFlare_invalidForm() throws Exception {

    doCallRealMethod().when(flareFormValidator).validate(any(), any());

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlare(
        ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/editFlareForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_EDIT)
        .containsEntry("flareTypes", FlareTestUtil.flareTypesAsMap)
        .containsEntry("cancelUrl", expectBaseFlaresUrl);
    assertThat(model.get("form").getClass()).isEqualTo(FlareForm.class);
    assertThat((FlareForm) model.get("form"))
        .extracting(FlareForm::getFlareType,
            flareForm -> flareForm.getDescription().getInputValue(),
            FlareForm::getMeteredFlag,
            flareForm -> flareForm.getCommentsMeteredYes().getInputValue(),
            flareForm -> flareForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(null, null, null, null, null);
  }

  @Test
  @WithMockUser
  void saveFlare_validForm() throws Exception {

    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenReturn(FlareTestUtil.flareHp);

    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlare(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseFlaresUrl));

    ArgumentCaptor<Flare> flareArgumentCaptor = ArgumentCaptor.forClass(Flare.class);
    ArgumentCaptor<FlareForm> flareFormArgumentCaptor = ArgumentCaptor.forClass(FlareForm.class);
    verify(flareService, times(1))
        .updateFlareFromForm(flareArgumentCaptor.capture(), flareFormArgumentCaptor.capture());
  }

  @Test
  void saveFlare_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareController.class).saveFlare(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void deleteFlareConfirm_flareExists() throws Exception {
    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenReturn(FlareTestUtil.flareHp);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).deleteFlareConfirm(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/deleteFlare"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(FlareController.PAGE_TITLE_ATTR_NAME, FlareController.PAGE_NAME_DELETE)
        .containsEntry("submitUrl", expectBaseFlaresUrl + "/" + FlareTestUtil.flareNoHp + "/delete")
        .containsEntry("cancelUrl", expectBaseFlaresUrl);
    assertThat(model.get("flareView").getClass()).isEqualTo(FlareView.class);
    assertThat((FlareView) model.get("flareView"))
        .extracting(FlareView::getDisplayOrder,
            FlareView::getFlareNo,
            FlareView::getEditUrl,
            FlareView::getDeleteUrl,
            FlareView::getFlareType,
            FlareView::getDescription,
            FlareView::getMeteredFlag,
            FlareView::getComments)
        .containsExactly(FlareTestUtil.flareViewHp.getDisplayOrder(),
            FlareTestUtil.flareViewHp.getFlareNo(),
            FlareTestUtil.flareViewHp.getEditUrl(),
            FlareTestUtil.flareViewHp.getDeleteUrl(),
            FlareTestUtil.flareViewHp.getFlareType(),
            FlareTestUtil.flareViewHp.getDescription(),
            FlareTestUtil.flareViewHp.getMeteredFlag(),
            FlareTestUtil.flareViewHp.getComments());
  }

  @Test
  @WithMockUser
  void deleteFlareConfirm_noFlareExists() {

    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenThrow(new EntityNotFoundException("Flare with application_version_id 1 and flare_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(FlareController.class).deleteFlareConfirm(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp)))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Flare with application_version_id %s and flare_no %s not found"
            .formatted(applicationVersion.getId(), FlareTestUtil.flareNoHp));
  }

  @Test
  void deleteFlareConfirm_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).deleteFlareConfirm(
            ApplicationTestUtil.APPLICATION_ID, FlareTestUtil.flareNoHp))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void deleteFlare_flareExists() throws Exception {
    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenReturn(FlareTestUtil.flareHp);
    when(flareService.flaresExistForApplicationVersion(applicationVersion)).thenReturn(true);
    mockMvc.perform(
        post(ReverseRouter.route(on(FlareController.class).deleteFlare(
            ApplicationTestUtil.APPLICATION_ID, null, FlareTestUtil.flareNoHp)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseFlaresUrl));

    verify(flareService, times(1)).deleteFlare(FlareTestUtil.flareHp);
  }

  @Test
  @WithMockUser
  void deleteFlare_noFlareExists() {

    when(flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp))
        .thenThrow(new EntityNotFoundException("Flare with application_version_id 1 and flare_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(FlareController.class).deleteFlare(
            ApplicationTestUtil.APPLICATION_ID, null, FlareTestUtil.flareNoHp)))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Flare with application_version_id %s and flare_no %s not found"
            .formatted(applicationVersion.getId(), FlareTestUtil.flareNoHp));
  }

  @Test
  void deleteFlare_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FlareController.class).deleteFlare(
            ApplicationTestUtil.APPLICATION_ID, null, FlareTestUtil.flareNoHp))))
        .andExpect(status().isUnauthorized());
  }

}
