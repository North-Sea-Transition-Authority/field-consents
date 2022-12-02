package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

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

import java.time.LocalDate;
import java.time.Month;
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
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareShortTermController.class)
class FlareShortTermControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private FlareShortTermService flareShortTermService;

  @MockBean
  private FlareShortTermFormService flareShortTermFormService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareShortTermTestUtil.flareAppVersion;

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  @WithMockUser
  void getFlareShortTermForm() throws Exception {
    var stubFlareShortTermForm =
        FlareShortTermTestUtil.getStubFlareShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 11),
            LocalDate.of(2023, Month.JANUARY, 1));

    when(flareShortTermService.getFlareShortTermForm(applicationVersion)).thenReturn(stubFlareShortTermForm);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FlareShortTermController.class)
                .getFlareShortTermForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareShortTermForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("startDate", "11 Apr 2022")
        .containsEntry("endDate", "01 Jan 2023")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/flare-short-term/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((FlareShortTermForm) model.get("form"))
        .isEqualTo(stubFlareShortTermForm);
  }

  @Test
  void getFlareShortTermForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareShortTermController.class)
            .getFlareShortTermForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }


  @Test
  @WithMockUser
  void saveFlareShortTermForm_invalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new FlareShortTermForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));
    when(flareShortTermFormService.validate(any(), any())).thenReturn(bindingResult);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(FlareShortTermController.class)
                .saveFlareShortTermForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(csrf())
                .param("flareShortTermMonthForms[0].year", "2022")
                .param("flareShortTermMonthForms[0].month", "April")
                .param("flareShortTermMonthForms[0].consentDays", "10")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareShortTermForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsKey("startDate")
        .containsKey("endDate")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/flare-short-term/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  @WithMockUser
  void saveFlareShortTermForm_validForm() throws Exception {

    when(flareShortTermFormService.validate(any(), any()))
        .thenReturn(new BeanPropertyBindingResult(null, "form"));

    mockMvc.perform(post(ReverseRouter.route(on(FlareShortTermController.class)
            .saveFlareShortTermForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareShortTermForm> flareShortTermFormArgumentCaptor =
        ArgumentCaptor.forClass(FlareShortTermForm.class);
    verify(flareShortTermService, times(1))
        .saveFlareShortTerm(applicationVersionArgumentCaptor.capture(),
            flareShortTermFormArgumentCaptor.capture());
  }

  @Test
  void saveFlareShortTermForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareShortTermController.class)
            .saveFlareShortTermForm(ApplicationTestUtil.APPLICATION_ID, null, null))))
        .andExpect(status().isForbidden());
  }

}
