package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = SupportingInformationController.class)
class SupportingInformationControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private SupportingInformationService supportingInformationService;

  @MockBean
  private SupportingInformationFormValidator supportingInformationFormValidator;

  private ApplicationVersion applicationVersion;

  private SupportingInformationForm form;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);

    form = new SupportingInformationForm();
    when(supportingInformationService.getSupportingInformationForm(applicationVersion)).thenReturn(form);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(applicationVersion.getApplication());
  }

  @Test
  @WithMockUser
  void getSupportingInformationForm_withValidUserAndFlareApplication() throws Exception {
    Map<String, Object> model = getSupportingInformationFormModel();

    assertThat(model)
        .containsEntry("applicationType", "flaring")
        .containsEntry("erapInformationAllowed", true)
        .containsEntry("submitUrl", ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(APPLICATION_ID)))
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat((SupportingInformationForm) model.get("form"))
        .isEqualTo(form);
  }

  @Test
  @WithMockUser
  void getSupportingInformationForm_withValidUserAndVentApplication() throws Exception {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(applicationVersion.getApplication());

    Map<String, Object> model = getSupportingInformationFormModel();

    assertThat(model)
        .containsEntry("applicationType", "venting")
        .containsEntry("erapInformationAllowed", true)
        .containsEntry("submitUrl", ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(APPLICATION_ID)))
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat((SupportingInformationForm) model.get("form"))
        .isEqualTo(form);
  }

  @Test
  @WithMockUser
  void getSupportingInformationForm_withValidUserAndProductionApplication() throws Exception {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(applicationVersion.getApplication());

    Map<String, Object> model = getSupportingInformationFormModel();

    assertThat(model)
        .containsEntry("erapInformationAllowed", false)
        .containsEntry("submitUrl", ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(APPLICATION_ID)))
        .containsEntry("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID)));

    assertThat((SupportingInformationForm) model.get("form"))
        .isEqualTo(form);
  }

  @NotNull
  private Map<String, Object> getSupportingInformationFormModel() throws Exception {
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(SupportingInformationController.class)
                .getSupportingInformationForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/application/supportingInformationForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    return modelAndView.getModel();
  }

  @Test
  void getSupportingInformationForm_withUnauthorisedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SupportingInformationController.class)
            .getSupportingInformationForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void saveSupportingInformation_withValidUser() throws Exception {
    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<SupportingInformationForm> supportingInformationFormArgumentCaptor =
        ArgumentCaptor.forClass(SupportingInformationForm.class);

    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(APPLICATION_ID))));

    verify(supportingInformationService, times(1))
        .saveSupportingInformation(applicationVersionArgumentCaptor.capture(), supportingInformationFormArgumentCaptor.capture());
  }

  @Test
  void saveSupportingInformation_withUnauthorisedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void saveSupportingInformation_withEmptyForm() throws Exception {
    doCallRealMethod().when(supportingInformationFormValidator).validate(any(), any());
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(ApplicationTestUtil.APPLICATION_ID)).thenReturn(applicationVersion);

    mockMvc.perform(post(ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
            .param("notes", "")
            .param("erapNotes", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/supportingInformationForm"))
        .andReturn().getModelAndView();
  }
}