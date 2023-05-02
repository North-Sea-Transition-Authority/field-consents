package uk.co.nstauthority.fieldconsents.production.gasinjection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = GasInjectionController.class)
class GasInjectionControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private GasInjectionService gasInjectionService;

  @MockBean
  private ApplicationFlagService applicationFlagService;

  private ApplicationVersion applicationVersion;

  private static final String TASK_LIST_URL = "/applications/" + APPLICATION_ID + "/task-list/";

  private static final String GAS_INJECTION_VIEW = "fcs/production/gasInjectionForm";

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @Test
  void getGasInjectionForm_validUser() throws Exception {
    var expectedGasInjectionForm = GasInjectionTestUtil.gasInjectionFormStub;
    when(gasInjectionService.getGasInjectionForm(applicationVersion)).thenReturn(expectedGasInjectionForm);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(GasInjectionController.class).getGasInjectionForm(APPLICATION_ID)))
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(GAS_INJECTION_VIEW))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("cancelUrl", TASK_LIST_URL);
    assertThat((GasInjectionForm) model.get("form"))
        .isEqualTo(expectedGasInjectionForm);
  }

  @SecurityTest
  void getGasInjectionForm_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(GasInjectionController.class).getGasInjectionForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveGasInjectionForm_emptyForm() throws Exception {
    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(GasInjectionController.class)
            .saveGasInjectionForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(GAS_INJECTION_VIEW))
        .andReturn().getModelAndView();

    verifyNoInteractions(applicationFlagService);

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("cancelUrl", TASK_LIST_URL);
    assertThat((GasInjectionForm) model.get("form"))
        .usingRecursiveComparison()
        .isEqualTo(GasInjectionTestUtil.gasInjectionFormStub);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void saveGasInjectionForm_validForm(Boolean willGasBeInjected) throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(GasInjectionController.class)
            .saveGasInjectionForm(APPLICATION_ID, null, null)))
            .param("willGasBeInjected", willGasBeInjected.toString())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + TASK_LIST_URL));

    verify(applicationFlagService, times(1))
        .deleteApplicationFlag(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED);
    verify(applicationFlagService, times(1))
        .saveApplicationFlag(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED, willGasBeInjected);
  }

  @SecurityTest
  void saveGasInjectionForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(GasInjectionController.class)
            .saveGasInjectionForm(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}