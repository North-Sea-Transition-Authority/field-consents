package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

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
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil.FIRST_MONTH_REPORTING_PERIOD;
import static uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil.LAST_MONTH_REPORTING_PERIOD;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataFormValidator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = VentReportGasDataController.class)
class VentReportGasDataControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private VentReportGasDataService ventReportGasDataService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  @MockBean
  private FlareVentReportGasDataFormValidator flareVentReportGasDataFormValidator;

  @MockBean
  private VentReportPeriodService ventReportPeriodService;

  private FlareVentReportGasDataForm form;

  @BeforeEach
  void setUp() {
    ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    form = new FlareVentReportGasDataForm();
    VentReportPeriod ventReportPeriod = new VentReportPeriod(applicationVersion, Month.OCTOBER, 2022);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(ventReportGasDataService.getFlareVentReportGasDataForm(applicationVersion)).thenReturn(form);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(ventReportPeriodService.getVentReportPeriodOrError(applicationVersion)).thenReturn(ventReportPeriod);
    when(applicationUnitService.getVentGasDensityUnit(applicationVersion)).thenReturn(FlareVentUnit.KG_PER_CUBIC_METER);
    when(applicationUnitService.getVentGasContentUnit(applicationVersion)).thenReturn(FlareVentUnit.MASS_PERCENTAGE);
  }

  @Test
  void getFlareReportGasDataForm() throws Exception {
    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(VentReportGasDataController.class)
                .getVentReportGasDataForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventReportGasDataForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportPeriodStart", FIRST_MONTH_REPORTING_PERIOD)
        .containsEntry("reportPeriodEnd", LAST_MONTH_REPORTING_PERIOD)
        .containsEntry("standardDensityUnit", FlareVentUnit.KG_PER_CUBIC_METER.getDisplayName())
        .containsEntry("gasContentUnit", FlareVentUnit.MASS_PERCENTAGE.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/vent-report-gas-properties")
        .containsEntry("cancelUrl", "/applications/1/task-list");

    assertThat((FlareVentReportGasDataForm) model.get("form"))
        .isEqualTo(form);
  }

  @SecurityTest
  void getFlareReportGasDataForm_unauthorisedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(VentReportGasDataController.class)
            .getVentReportGasDataForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveFlareReportGasDataForm_withValidForm() throws Exception {
    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareVentReportGasDataForm> flareVentReportGasDataFormCaptor =
        ArgumentCaptor.forClass(FlareVentReportGasDataForm.class);

    mockMvc.perform(post(ReverseRouter.route(on(VentReportGasDataController.class)
            .saveVentReportGasDataForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));

    verify(ventReportGasDataService, times(1))
        .saveVentReportGasData(applicationVersionArgumentCaptor.capture(), flareVentReportGasDataFormCaptor.capture());
  }

  @Test
  void saveFlareReportGasDataForm_withNotValidForm() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("categoryADensity", "required", "Enter Category A Standard density");
      return null;
    })
        .when(flareVentReportGasDataFormValidator)
        .validate(any(FlareVentReportGasDataForm.class), any(BindingResult.class));

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(VentReportGasDataController.class)
                .saveVentReportGasDataForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventReportGasDataForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportPeriodStart", FIRST_MONTH_REPORTING_PERIOD)
        .containsEntry("reportPeriodEnd", LAST_MONTH_REPORTING_PERIOD)
        .containsEntry("standardDensityUnit", FlareVentUnit.KG_PER_CUBIC_METER.getDisplayName())
        .containsEntry("gasContentUnit", FlareVentUnit.MASS_PERCENTAGE.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/vent-report-gas-properties")
        .containsEntry("cancelUrl", "/applications/1/task-list");
  }

  @SecurityTest
  void saveFlareReportGasDataForm_unauthorisedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentReportGasDataController.class)
            .saveVentReportGasDataForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}
