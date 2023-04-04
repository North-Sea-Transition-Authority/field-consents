package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

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
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = VentController.class)
class VentControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private VentService ventService;

  @MockBean
  private VentSummaryService ventSummaryService;

  @MockBean
  private VentFormValidator ventFormValidator;

  @MockBean
  private VentSetupFormValidator ventSetupFormValidator;

  private ApplicationVersion applicationVersion;

  private String expectBaseVentsUrl;

  @BeforeEach
  void setUp() {
    applicationVersion = VentTestUtil.ventAppVersion;
    expectBaseVentsUrl = VentTestUtil.BASE_VENTS_URL;
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  void addVent_validUser() throws Exception {
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).addVent(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/editVentForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_ADD)
        .containsEntry("ventTypes", VentTestUtil.ventTypesAsMap)
        .containsEntry("cancelUrl", expectBaseVentsUrl);
    assertThat((VentForm) model.get("form"))
        .extracting(VentForm::getVentType,
            ventForm -> ventForm.getDescription().getInputValue(),
            VentForm::getMeteredFlag,
            ventForm -> ventForm.getCommentsMeteredYes().getInputValue(),
            ventForm -> ventForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(null, null, null, null, null);
  }

  @Test
  void addVent_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(VentController.class).addVent(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void saveNewVent_invalidForm() throws Exception {

    doCallRealMethod().when(ventFormValidator).validate(any(), any());

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveNewVent(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/editVentForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_ADD)
        .containsEntry("ventTypes", VentTestUtil.ventTypesAsMap)
        .containsEntry("cancelUrl", expectBaseVentsUrl);
    assertThat((VentForm) model.get("form"))
        .extracting(VentForm::getVentType,
            ventForm -> ventForm.getDescription().getInputValue(),
            VentForm::getMeteredFlag,
            ventForm -> ventForm.getCommentsMeteredYes().getInputValue(),
            ventForm -> ventForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(null, null, null, null, null);
  }

  @Test
  @WithMockUser
  void saveNewVent_validForm() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveNewVent(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseVentsUrl));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<VentForm> ventFormArgumentCaptor = ArgumentCaptor.forClass(VentForm.class);
    verify(ventService, times(1))
        .saveNewVent(applicationVersionArgumentCaptor.capture(), ventFormArgumentCaptor.capture());
  }

  @Test
  void saveNewVent_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveNewVent(
        ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void viewVentsSummary_noVents() throws Exception {
    when(ventService.ventsExistForApplicationVersion(applicationVersion)).thenReturn(Boolean.FALSE);

    mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).viewVentsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  @WithMockUser
  void viewVentsSummary_ventsExist() throws Exception {
    when(ventService.ventsExistForApplicationVersion(applicationVersion)).thenReturn(Boolean.TRUE);
    when(ventSummaryService.getVentViews(applicationVersion)).thenReturn(VentTestUtil.ventViews);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(VentController.class).viewVentsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/ventsSummaryForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_SUMMARY)
        .containsEntry("ventViews", VentTestUtil.ventViews)
        .containsEntry("submitUrl", expectBaseVentsUrl);
    assertThat((VentSetupForm) model.get("form"))
        .extracting(VentSetupForm::getHasOtherVentsToAdd)
        .isNull();
  }

  @Test
  void viewVentsSummary_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).viewVentsSummary(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void saveVentsSummary_invalidForm() throws Exception {

    doCallRealMethod().when(ventSetupFormValidator).validate(any(), any());
    when(ventSummaryService.getVentViews(applicationVersion)).thenReturn(VentTestUtil.ventViews);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVentsSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/ventsSummaryForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_SUMMARY)
        .containsEntry("ventViews", VentTestUtil.ventViews)
        .containsEntry("submitUrl", expectBaseVentsUrl);
    assertThat((VentSetupForm) model.get("form"))
        .extracting(VentSetupForm::getHasOtherVentsToAdd)
        .isNull();
  }

  @Test
  @WithMockUser
  void saveVentsSummary_validFormMoreVents() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVentsSummary(
        ApplicationTestUtil.APPLICATION_ID,null, null)))
            .param("hasOtherVentsToAdd", Boolean.TRUE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseVentsUrl + "/new"));
  }

  @Test
  @WithMockUser
  void saveVentsSummary_validFormNoMoreVents() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVentsSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null)))
            .param("hasOtherVentsToAdd", Boolean.FALSE.toString())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  void saveVentsSummary_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVentsSummary(
        ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void editVent_ventFound() throws Exception {

    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenReturn(VentTestUtil.ventHp);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).editVent(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/editVentForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_EDIT)
        .containsEntry("ventTypes", VentTestUtil.ventTypesAsMap)
        .containsEntry("cancelUrl", expectBaseVentsUrl);
    assertThat((VentForm) model.get("form"))
        .extracting(VentForm::getVentType,
            ventForm -> ventForm.getDescription().getInputValue(),
            VentForm::getMeteredFlag,
            ventForm -> ventForm.getCommentsMeteredYes().getInputValue(),
            ventForm -> ventForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(VentTestUtil.ventHp.getVentType(),
            VentTestUtil.ventHp.getDescription(),
            VentTestUtil.ventHp.getMeteredFlag(),
            VentTestUtil.ventHp.getComments(),
            null);
  }

  @Test
  @WithMockUser
  void editVent_noVentFound() {

    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenThrow(new EntityNotFoundException("Vent with application_version_id 1 and vent_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(VentController.class).editVent(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp)))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Vent with application_version_id %s and vent_no %s not found"
            .formatted(applicationVersion.getId(), VentTestUtil.ventNoHp));
  }

  @Test
  void editVent_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).editVent(applicationVersion.getApplication().getId(),
                VentTestUtil.ventNoHp))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void saveVent_invalidForm() throws Exception {

    doCallRealMethod().when(ventFormValidator).validate(any(), any());

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVent(
        ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp, null, null)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/editVentForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_EDIT)
        .containsEntry("ventTypes", VentTestUtil.ventTypesAsMap)
        .containsEntry("cancelUrl", expectBaseVentsUrl);
    assertThat(model.get("form").getClass()).isEqualTo(VentForm.class);
    assertThat((VentForm) model.get("form"))
        .extracting(VentForm::getVentType,
            ventForm -> ventForm.getDescription().getInputValue(),
            VentForm::getMeteredFlag,
            ventForm -> ventForm.getCommentsMeteredYes().getInputValue(),
            ventForm -> ventForm.getCommentsMeteredNo().getInputValue())
        .containsExactly(null, null, null, null, null);
  }

  @Test
  @WithMockUser
  void saveVent_validForm() throws Exception {

    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenReturn(VentTestUtil.ventHp);

    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVent(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + expectBaseVentsUrl));

    ArgumentCaptor<Vent> ventArgumentCaptor = ArgumentCaptor.forClass(Vent.class);
    ArgumentCaptor<VentForm> ventFormArgumentCaptor = ArgumentCaptor.forClass(VentForm.class);
    verify(ventService, times(1))
        .updateVentFromForm(ventArgumentCaptor.capture(), ventFormArgumentCaptor.capture());
  }

  @Test
  void saveVent_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentController.class).saveVent(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void deleteVentConfirm_ventExists() throws Exception {
    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenReturn(VentTestUtil.ventHp);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).deleteVentConfirm(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/vent/deleteVent"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry(VentController.PAGE_TITLE_ATTR_NAME, VentController.PAGE_NAME_DELETE)
        .containsEntry("submitUrl", expectBaseVentsUrl + "/" + VentTestUtil.ventNoHp + "/delete")
        .containsEntry("cancelUrl", expectBaseVentsUrl);
    assertThat(model.get("ventView").getClass()).isEqualTo(VentView.class);
    assertThat((VentView) model.get("ventView"))
        .extracting(VentView::getDisplayOrder,
            VentView::getVentNo,
            VentView::getEditUrl,
            VentView::getDeleteUrl,
            VentView::getVentType,
            VentView::getDescription,
            VentView::getMeteredFlag,
            VentView::getComments)
        .containsExactly(VentTestUtil.ventViewHp.getDisplayOrder(),
            VentTestUtil.ventViewHp.getVentNo(),
            VentTestUtil.ventViewHp.getEditUrl(),
            VentTestUtil.ventViewHp.getDeleteUrl(),
            VentTestUtil.ventViewHp.getVentType(),
            VentTestUtil.ventViewHp.getDescription(),
            VentTestUtil.ventViewHp.getMeteredFlag(),
            VentTestUtil.ventViewHp.getComments());
  }

  @Test
  @WithMockUser
  void deleteVentConfirm_noVentExists() {

    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenThrow(new EntityNotFoundException("Vent with application_version_id 1 and vent_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(VentController.class).deleteVentConfirm(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp)))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Vent with application_version_id %s and vent_no %s not found"
            .formatted(applicationVersion.getId(), VentTestUtil.ventNoHp));
  }

  @Test
  void deleteVentConfirm_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).deleteVentConfirm(
            ApplicationTestUtil.APPLICATION_ID, VentTestUtil.ventNoHp))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void deleteVent_ventExists() throws Exception {
    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenReturn(VentTestUtil.ventHp);

    mockMvc.perform(
        post(ReverseRouter.route(on(VentController.class).deleteVent(
            ApplicationTestUtil.APPLICATION_ID, null, VentTestUtil.ventNoHp)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    verify(ventService, times(1)).deleteVent(VentTestUtil.ventHp);
  }

  @Test
  @WithMockUser
  void deleteVent_noVentExists() {

    when(ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp))
        .thenThrow(new EntityNotFoundException("Vent with application_version_id 1 and vent_no 1 not found"));

    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(VentController.class).deleteVent(
            ApplicationTestUtil.APPLICATION_ID, null, VentTestUtil.ventNoHp)))))
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Vent with application_version_id %s and vent_no %s not found"
            .formatted(applicationVersion.getId(), VentTestUtil.ventNoHp));
  }

  @Test
  void deleteVent_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(VentController.class).deleteVent(
            ApplicationTestUtil.APPLICATION_ID, null, VentTestUtil.ventNoHp))))
        .andExpect(redirectionToLoginUrl());
  }

}
