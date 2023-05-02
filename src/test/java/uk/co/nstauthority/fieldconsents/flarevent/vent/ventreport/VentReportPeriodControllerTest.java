package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

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
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Year;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodControllerHelperService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodFormValidator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = VentReportPeriodController.class)
class VentReportPeriodControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private VentReportPeriodService ventReportPeriodService;

  @MockBean
  private FlareVentReportPeriodFormValidator reportPeriodFormValidator;

  @MockBean
  private FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService;

  private FlareVentReportPeriodForm reportPeriodForm;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    reportPeriodForm = VentReportTestUtil.getFullVentReportPeriodForm();

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(reportPeriodControllerHelperService)
        .getReportEndYearsMap(applicationVersion);
  }

  @Test
  void getVentReportPeriodForm_validUser() throws Exception {
    when(ventReportPeriodService.getVentReportPeriodForm(applicationVersion))
        .thenReturn(reportPeriodForm);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(VentReportPeriodController.class)
                .getVentReportPeriodForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventReportPeriodForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportEndMonthsMap", DateUtils.monthsMap())
        .containsEntry("reportEndYearsMap", Map.of(
            Year.now().minusYears(1).toString(),
            Year.now().minusYears(1).toString(),
            Year.now().toString(), Year.now().toString()
            ))
        .containsEntry("submitUrl", "/applications/1/vent-report/period/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((FlareVentReportPeriodForm) model.get("form"))
        .isEqualTo(reportPeriodForm);
  }

  @SecurityTest
  void getVentReportPeriodForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(VentReportPeriodController.class)
            .getVentReportPeriodForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveVentReportPeriodForm_invalidForm() throws Exception {

    doCallRealMethod().when(reportPeriodFormValidator).validate(any(), any());

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(VentReportPeriodController.class)
                .saveVentReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventReportPeriodForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportEndMonthsMap", DateUtils.monthsMap())
        .containsEntry("reportEndYearsMap", Map.of(
            Year.now().minusYears(1).toString(),
            Year.now().minusYears(1).toString(),
            Year.now().toString(), Year.now().toString()
        ))
        .containsEntry("submitUrl", "/applications/1/vent-report/period/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  void saveVentReportPeriodForm_validForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(VentReportPeriodController.class)
            .saveVentReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/vent-report/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareVentReportPeriodForm> ventReportPeriodFormArgumentCaptor =
        ArgumentCaptor.forClass(FlareVentReportPeriodForm.class);
    verify(ventReportPeriodService, times(1))
        .saveVentReportPeriod(applicationVersionArgumentCaptor.capture(),
            ventReportPeriodFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveVentReportPeriodForm_noUser() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(VentReportPeriodController.class)
            .saveVentReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }
}