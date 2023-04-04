package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
import static uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil.FIRST_MONTH_REPORTING_PERIOD;
import static uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil.LAST_MONTH_REPORTING_PERIOD;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataFormValidator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareReportGasDataController.class)
class FlareReportGasDataControllerTest extends AbstractControllerTest {

  @MockBean
  private FlareReportGasDataService flareReportGasDataService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  @MockBean
  private FlareVentReportGasDataFormValidator flareVentReportGasDataFormValidator;

  @MockBean
  private FlareReportPeriodService flareReportPeriodService;

  private FlareVentReportGasDataForm form;

  @BeforeEach
  void setUp() {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    form = new FlareVentReportGasDataForm();
    FlareReportPeriod flareReportPeriod = new FlareReportPeriod(applicationVersion, Month.OCTOBER, 2022);

    when(flareReportGasDataService.getFlareVentReportGasDataForm(applicationVersion)).thenReturn(form);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion)).thenReturn(flareReportPeriod);
    when(applicationUnitService.getFlareGasDensityUnit(applicationVersion)).thenReturn(FlareVentUnit.KG_PER_CUBIC_METER);
    when(applicationUnitService.getFlareGasContentUnit(applicationVersion)).thenReturn(FlareVentUnit.MASS_PERCENTAGE);
  }

  @Test
  @WithMockUser
  void getFlareReportGasDataForm() throws Exception {
    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FlareReportGasDataController.class)
                .getFlareReportGasDataForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportGasDataForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportPeriodStart", FIRST_MONTH_REPORTING_PERIOD)
        .containsEntry("reportPeriodEnd", LAST_MONTH_REPORTING_PERIOD)
        .containsEntry("standardDensityUnit", FlareVentUnit.KG_PER_CUBIC_METER.getDisplayName())
        .containsEntry("gasContentUnit", FlareVentUnit.MASS_PERCENTAGE.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/flare-report-gas-properties/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((FlareVentReportGasDataForm) model.get("form"))
        .isEqualTo(form);
  }

  @Test
  void getFlareReportGasDataForm_unauthorisedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareReportGasDataController.class)
        .getFlareReportGasDataForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void saveFlareReportGasDataForm_withValidForm() throws Exception {
    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareVentReportGasDataForm> flareVentReportGasDataFormCaptor =
        ArgumentCaptor.forClass(FlareVentReportGasDataForm.class);

    mockMvc.perform(post(ReverseRouter.route(on(FlareReportGasDataController.class)
            .saveFlareReportGasDataForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    verify(flareReportGasDataService, times(1))
        .saveFlareReportGasData(applicationVersionArgumentCaptor.capture(), flareVentReportGasDataFormCaptor.capture());
  }

  @Test
  @WithMockUser
  void saveFlareReportGasDataForm_withNotValidForm() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("categoryADensity", "required", "Enter Category A Standard density");
      return null;
    })
        .when(flareVentReportGasDataFormValidator)
        .validate(any(FlareVentReportGasDataForm.class), any(BindingResult.class));

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(FlareReportGasDataController.class)
            .saveFlareReportGasDataForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
            .andExpect(status().isOk())
        .andExpect(view().name("fcs/flare/flareReportGasDataForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportPeriodStart", FIRST_MONTH_REPORTING_PERIOD)
        .containsEntry("reportPeriodEnd", LAST_MONTH_REPORTING_PERIOD)
        .containsEntry("standardDensityUnit", FlareVentUnit.KG_PER_CUBIC_METER.getDisplayName())
        .containsEntry("gasContentUnit", FlareVentUnit.MASS_PERCENTAGE.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/flare-report-gas-properties/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  void saveFlareReportGasDataForm_unauthorisedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareReportGasDataController.class)
        .saveFlareReportGasDataForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}