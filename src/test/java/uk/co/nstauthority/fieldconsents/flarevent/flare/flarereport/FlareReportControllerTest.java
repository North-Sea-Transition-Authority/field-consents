package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareReportController.class)
class FlareReportControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private FlareReportService flareReportService;

  @MockBean
  private FlareReportPeriodService flareReportPeriodService;

  @MockBean
  private FlareReportFormService flareReportFormService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareReportTestUtil.flareAppVersion;

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  void getFlareReportForm() throws Exception {
    var stubFlareReportForm = FlareReportTestUtil.getStubFlareReportForm();

    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(true);
    when(flareReportService.getFlareReportForm(applicationVersion)).thenReturn(stubFlareReportForm);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FlareReportController.class)
                .getFlareReportForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Flare report 2022/2023")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("periodUrl", "/applications/1/flare-report/period/")
        .containsEntry("submitUrl", "/applications/1/flare-report/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((FlareReportForm) model.get("form"))
        .isEqualTo(stubFlareReportForm);
  }

  @Test
  @WithMockUser
  void getFlareReportForm_periodNotExists() throws Exception {

    when(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FlareReportController.class)
            .getFlareReportForm(ApplicationTestUtil.APPLICATION_ID)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/flare-report/period/"));

  }

  @Test
  void getFlareReportForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareReportController.class)
            .getFlareReportForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }


  @Test
  @WithMockUser
  void saveFlareReportForm_invalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new FlareReportForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));
    when(flareReportFormService.validate(any(), any())).thenReturn(bindingResult);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(FlareReportController.class)
                .saveFlareReportForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(csrf())
                .param("flareReportMonthForms[0].year", "2022")
                .param("flareReportMonthForms[0].month", "November")
                .param("flareReportMonthForms[1].year", "2022")
                .param("flareReportMonthForms[1].month", "December")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Flare report 2022")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("periodUrl", "/applications/1/flare-report/period/")
        .containsEntry("submitUrl", "/applications/1/flare-report/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  @WithMockUser
  void saveFlareReportForm_validForm() throws Exception {

    when(flareReportFormService.validate(any(), any()))
        .thenReturn(new BeanPropertyBindingResult(null, "form"));

    mockMvc.perform(post(ReverseRouter.route(on(FlareReportController.class)
            .saveFlareReportForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareReportForm> flareReportFormArgumentCaptor =
        ArgumentCaptor.forClass(FlareReportForm.class);
    verify(flareReportService, times(1))
        .saveFlareReport(applicationVersionArgumentCaptor.capture(),
            flareReportFormArgumentCaptor.capture());
  }

  @Test
  void saveFlareReportForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareReportController.class)
            .saveFlareReportForm(ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

}
