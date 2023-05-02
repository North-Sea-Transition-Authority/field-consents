package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodControllerHelperService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodFormValidator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareReportPeriodController.class)
class FlareReportPeriodControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private FlareReportPeriodService flareReportPeriodService;

  @MockBean
  private FlareVentReportPeriodFormValidator reportPeriodFormValidator;

  @MockBean
  private FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService;

  private FlareVentReportPeriodForm reportPeriodForm;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareReportTestUtil.flareAppVersion;
    reportPeriodForm = FlareReportTestUtil.getFullFlareReportPeriodForm();

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(reportPeriodControllerHelperService)
        .getReportEndYearsMap(applicationVersion);
  }

  @Test
  void getFlareReportPeriodForm_validUser() throws Exception {
    when(flareReportPeriodService.getFlareReportPeriodForm(applicationVersion))
        .thenReturn(reportPeriodForm);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FlareReportPeriodController.class)
                .getFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportPeriodForm"))
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
        .containsEntry("submitUrl", "/applications/1/flare-report/period/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((FlareVentReportPeriodForm) model.get("form"))
        .isEqualTo(reportPeriodForm);
  }

  @SecurityTest
  void getFlareReportPeriodForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveFlareReportPeriodForm_invalidForm() throws Exception {

    doCallRealMethod().when(reportPeriodFormValidator).validate(any(), any());

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(FlareReportPeriodController.class)
                .saveFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportPeriodForm"))
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
        .containsEntry("submitUrl", "/applications/1/flare-report/period/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  void saveFlareReportPeriodForm_validForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(FlareReportPeriodController.class)
            .saveFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/flare-report/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareVentReportPeriodForm> flareReportPeriodFormArgumentCaptor =
        ArgumentCaptor.forClass(FlareVentReportPeriodForm.class);
    verify(flareReportPeriodService, times(1))
        .saveFlareReportPeriod(applicationVersionArgumentCaptor.capture(),
            flareReportPeriodFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveFlareReportPeriodForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareReportPeriodController.class)
            .saveFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }
}