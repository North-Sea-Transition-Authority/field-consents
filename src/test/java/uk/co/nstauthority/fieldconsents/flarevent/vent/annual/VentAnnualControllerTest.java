package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@ContextConfiguration(classes = VentAnnualController.class)
class VentAnnualControllerTest extends AbstractApplicationControllerTest {

  @MockitoBean
  private VentAnnualService ventAnnualService;

  @MockitoBean
  private VentAnnualFormService ventAnnualFormService;

  @MockitoBean
  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = VentAnnualTestUtil.ventAppVersion;

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void getVentAnnualForm() throws Exception {
    var stubVentAnnualForm = VentAnnualTestUtil.getStubVentAnnualFormForYear(2022);

    when(ventAnnualService.getVentAnnualForm(applicationVersion)).thenReturn(stubVentAnnualForm);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(VentAnnualController.class)
                .getVentAnnualForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventAnnualForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Annual consent 2022")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/vent-annual")
        .containsEntry("cancelUrl", "/applications/1/task-list");

    assertThat((VentAnnualForm) model.get("form"))
        .isEqualTo(stubVentAnnualForm);
  }

  @SecurityTest
  void getVentAnnualForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(VentAnnualController.class)
            .getVentAnnualForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }


  @Test
  void saveVentAnnualForm_invalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new VentAnnualForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));
    when(ventAnnualFormService.validate(any(), any())).thenReturn(bindingResult);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(VentAnnualController.class)
                .saveVentAnnualForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf())
                .param("ventAnnualMonthForms[0].year", "2022")
                .param("ventAnnualMonthForms[0].month", "January")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/vent/ventAnnualForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Annual consent 2022")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/vent-annual")
        .containsEntry("cancelUrl", "/applications/1/task-list");
  }

  @Test
  void saveVentAnnualForm_validForm() throws Exception {

    when(ventAnnualFormService.validate(any(), any()))
        .thenReturn(new BeanPropertyBindingResult(null, "form"));

    mockMvc.perform(post(ReverseRouter.route(on(VentAnnualController.class)
            .saveVentAnnualForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<VentAnnualForm> ventAnnualFormArgumentCaptor =
        ArgumentCaptor.forClass(VentAnnualForm.class);
    verify(ventAnnualService, times(1))
        .saveVentAnnual(applicationVersionArgumentCaptor.capture(),
            ventAnnualFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveVentAnnualForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(VentAnnualController.class)
            .saveVentAnnualForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }
}
