package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

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

import java.time.LocalDate;
import java.time.Month;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = VentShortTermController.class)
class VentShortTermControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private VentShortTermService ventShortTermService;

  @MockBean
  private VentShortTermFormService ventShortTermFormService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = VentShortTermTestUtil.ventAppVersion;

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void getVentShortTermForm() throws Exception {
    var stubVentShortTermForm =
        VentShortTermTestUtil.getStubVentShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 11),
            LocalDate.of(2023, Month.JANUARY, 1));

    when(ventShortTermService.getVentShortTermForm(applicationVersion)).thenReturn(stubVentShortTermForm);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(VentShortTermController.class)
                .getVentShortTermForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventShortTermForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("startDate", "11 Apr 2022")
        .containsEntry("endDate", "1 Jan 2023")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/vent-short-term/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");

    assertThat((VentShortTermForm) model.get("form"))
        .isEqualTo(stubVentShortTermForm);
  }

  @SecurityTest
  void getVentShortTermForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(VentShortTermController.class)
            .getVentShortTermForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }


  @Test
  void saveVentShortTermForm_invalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new VentShortTermForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));
    when(ventShortTermFormService.validate(any(), any())).thenReturn(bindingResult);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(VentShortTermController.class)
                .saveVentShortTermForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf())
                .param("ventShortTermMonthForms[0].year", "2022")
                .param("ventShortTermMonthForms[0].month", "April")
                .param("ventShortTermMonthForms[0].consentDays", "10")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventShortTermForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsKey("startDate")
        .containsKey("endDate")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/vent-short-term/")
        .containsEntry("cancelUrl", "/applications/1/task-list/");
  }

  @Test
  void saveVentShortTermForm_validForm() throws Exception {

    when(ventShortTermFormService.validate(any(), any()))
        .thenReturn(new BeanPropertyBindingResult(null, "form"));

    mockMvc.perform(post(ReverseRouter.route(on(VentShortTermController.class)
            .saveVentShortTermForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<VentShortTermForm> ventShortTermFormArgumentCaptor =
        ArgumentCaptor.forClass(VentShortTermForm.class);
    verify(ventShortTermService, times(1))
        .saveVentShortTerm(applicationVersionArgumentCaptor.capture(),
            ventShortTermFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveVentShortTermForm_noUser() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(VentShortTermController.class)
            .saveVentShortTermForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }
}