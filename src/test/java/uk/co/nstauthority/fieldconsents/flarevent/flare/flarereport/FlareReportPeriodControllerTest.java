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

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Map;
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
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareReportPeriodController.class)
class FlareReportPeriodControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private FlareReportPeriodService flareReportPeriodService;

  @MockBean
  private FlareReportPeriodFormValidator flareReportPeriodFormValidator;

  @MockBean
  private FlareReportPeriodControllerHelperService flareReportPeriodControllerHelperService;

  @MockBean
  private FlareReportPeriodHelperService flareReportPeriodHelperService;

  private FlareReportPeriodForm flareReportPeriodForm;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareReportTestUtil.flareAppVersion;
    flareReportPeriodForm = FlareReportTestUtil.getFullFlareReportPeriodForm();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    doCallRealMethod().when(flareReportPeriodControllerHelperService)
        .getReportEndYearsMap(applicationVersion);
  }

  @Test
  @WithMockUser
  void getFlareReportPeriodForm_validUser() throws Exception {
    when(flareReportPeriodService.getFlareReportPeriodForm(applicationVersion))
        .thenReturn(flareReportPeriodForm);
    when(flareReportPeriodHelperService.getProposedReportStartYearMonth(applicationVersion))
        .thenReturn(YearMonth.of(2022, Month.JUNE));
    when(flareReportPeriodHelperService.getProposedReportEndYearMonth(applicationVersion))
        .thenReturn(YearMonth.of(2023, Month.MAY));

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FlareReportPeriodController.class)
                .getFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportPeriodForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportPeriodStart", "June 2022")
        .containsEntry("reportPeriodEnd", "May 2023")
        .containsEntry("reportEndMonthsMap", DateUtils.monthsMap())
        .containsEntry("reportEndYearsMap", Map.of(
            Year.now().minusYears(1).toString(),
            Year.now().minusYears(1).toString(),
            Year.now().toString(), Year.now().toString()
            ))
        .containsEntry("submitUrl", "/applications/1/flare-report/period/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((FlareReportPeriodForm) model.get("form"))
        .isEqualTo(flareReportPeriodForm);
  }

  @Test
  void getFlareReportPeriodForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveFlareReportPeriodForm_invalidForm() throws Exception {

    when(flareReportPeriodHelperService.getProposedReportStartYearMonth(applicationVersion))
        .thenReturn(YearMonth.of(2022, Month.JUNE));
    when(flareReportPeriodHelperService.getProposedReportEndYearMonth(applicationVersion))
        .thenReturn(YearMonth.of(2023, Month.MAY));

    doCallRealMethod().when(flareReportPeriodFormValidator).validate(any(), any());

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(FlareReportPeriodController.class)
                .saveFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareReportPeriodForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("reportPeriodStart", "June 2022")
        .containsEntry("reportPeriodEnd", "May 2023")
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
  @WithMockUser
  void saveFlareReportPeriodForm_validForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(FlareReportPeriodController.class)
            .saveFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/flare-report/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareReportPeriodForm> flareReportPeriodFormArgumentCaptor =
        ArgumentCaptor.forClass(FlareReportPeriodForm.class);
    verify(flareReportPeriodService, times(1))
        .saveFlareReportPeriod(applicationVersionArgumentCaptor.capture(),
            flareReportPeriodFormArgumentCaptor.capture());
  }

  @Test
  void saveFlareReportPeriodForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareReportPeriodController.class)
            .saveFlareReportPeriodForm(ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

}
