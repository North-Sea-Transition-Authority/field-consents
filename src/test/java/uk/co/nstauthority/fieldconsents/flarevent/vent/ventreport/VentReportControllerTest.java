package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = VentReportController.class)
class VentReportControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private VentReportService ventReportService;

  @MockBean
  private VentReportPeriodService ventReportPeriodService;

  @MockBean
  private VentReportFormService ventReportFormService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void getVentReportForm() throws Exception {
    var stubVentReportForm = VentReportTestUtil.getStubVentReportForm();

    when(ventReportPeriodService.ventReportPeriodExists(applicationVersion)).thenReturn(true);
    when(ventReportService.getVentReportForm(applicationVersion)).thenReturn(stubVentReportForm);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(VentReportController.class)
                .getVentReportForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventReportForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Vent report 2022/2023")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("periodUrl", "/applications/1/vent-report/period/")
        .containsEntry("submitUrl", "/applications/1/vent-report/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((VentReportForm) model.get("form"))
        .isEqualTo(stubVentReportForm);
  }

  @Test
  void getVentReportForm_periodNotExists() throws Exception {

    when(ventReportPeriodService.ventReportPeriodExists(applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(VentReportController.class)
            .getVentReportForm(ApplicationTestUtil.APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/vent-report/period/"));

  }

  @SecurityTest
  void getVentReportForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(VentReportController.class)
            .getVentReportForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveVentReportForm_invalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new VentReportForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));
    when(ventReportFormService.validate(any(), any())).thenReturn(bindingResult);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(VentReportController.class)
                .saveVentReportForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf())
                .param("ventReportMonthForms[0].year", "2022")
                .param("ventReportMonthForms[0].month", "November")
                .param("ventReportMonthForms[1].year", "2022")
                .param("ventReportMonthForms[1].month", "December")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventReportForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Vent report 2022")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("periodUrl", "/applications/1/vent-report/period/")
        .containsEntry("submitUrl", "/applications/1/vent-report/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  void saveVentReportForm_validForm() throws Exception {

    when(ventReportFormService.validate(any(), any()))
        .thenReturn(new BeanPropertyBindingResult(null, "form"));

    mockMvc.perform(post(ReverseRouter.route(on(VentReportController.class)
            .saveVentReportForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<VentReportForm> ventReportFormArgumentCaptor =
        ArgumentCaptor.forClass(VentReportForm.class);
    verify(ventReportService, times(1))
        .saveVentReport(applicationVersionArgumentCaptor.capture(),
            ventReportFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveVentReportForm_noUser() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(VentReportController.class)
            .saveVentReportForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }
}