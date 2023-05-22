package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FlareAnnualController.class)
class FlareAnnualControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private FlareAnnualService flareAnnualService;

  @MockBean
  private FlareAnnualFormService flareAnnualFormService;

  @MockBean
  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareAnnualTestUtil.flareAppVersion;

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void getFlareAnnualForm() throws Exception {
    var stubFlareAnnualForm = FlareAnnualTestUtil.getStubFlareAnnualFormForYear(2022);

    when(flareAnnualService.getFlareAnnualForm(applicationVersion)).thenReturn(stubFlareAnnualForm);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(FlareAnnualController.class)
                .getFlareAnnualForm(ApplicationTestUtil.APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareAnnualForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Annual consent 2022")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/flare-annual")
        .containsEntry("cancelUrl", "/applications/1/task-list");

    assertThat((FlareAnnualForm) model.get("form"))
        .isEqualTo(stubFlareAnnualForm);
  }

  @SecurityTest
  void getFlareAnnualForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FlareAnnualController.class)
            .getFlareAnnualForm(ApplicationTestUtil.APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }


  @Test
  void saveFlareAnnualForm_invalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new FlareAnnualForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));
    when(flareAnnualFormService.validate(any(), any())).thenReturn(bindingResult);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion)).thenReturn(FlareVentUnit.TONNES_PER_MONTH);

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(FlareAnnualController.class)
                .saveFlareAnnualForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
                .with(user(user))
                .with(csrf())
                .param("flareAnnualMonthForms[0].year", "2022")
                .param("flareAnnualMonthForms[0].month", "January")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/flare/flareAnnualForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Annual consent 2022")
        .containsEntry("categoryUnit", FlareVentUnit.TONNES_PER_MONTH.getDisplayName())
        .containsEntry("submitUrl", "/applications/1/flare-annual")
        .containsEntry("cancelUrl", "/applications/1/task-list");
  }

  @Test
  void saveFlareAnnualForm_validForm() throws Exception {

    when(flareAnnualFormService.validate(any(), any()))
        .thenReturn(new BeanPropertyBindingResult(null, "form"));

    mockMvc.perform(post(ReverseRouter.route(on(FlareAnnualController.class)
            .saveFlareAnnualForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<FlareAnnualForm> flareAnnualFormArgumentCaptor =
        ArgumentCaptor.forClass(FlareAnnualForm.class);
    verify(flareAnnualService, times(1))
        .saveFlareAnnual(applicationVersionArgumentCaptor.capture(),
            flareAnnualFormArgumentCaptor.capture());
  }

  @SecurityTest
  void saveFlareAnnualForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FlareAnnualController.class)
            .saveFlareAnnualForm(ApplicationTestUtil.APPLICATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }
}
