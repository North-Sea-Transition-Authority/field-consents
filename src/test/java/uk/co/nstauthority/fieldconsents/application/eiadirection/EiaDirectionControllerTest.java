package uk.co.nstauthority.fieldconsents.application.eiadirection;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
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
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_REF_1;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = EiaDirectionController.class)
class EiaDirectionControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private EiaDirectionService eiaDirectionService;

  @MockBean
  private EiaDirectionFormValidator formValidator;

  @MockBean
  private EiaDirectionFormService eiaDirectionFormService;

  private ApplicationVersion applicationVersion;

  private static final String TASK_LIST_URL = "/applications/" + APPLICATION_ID + "/task-list/";

  private static final String EIA_DIRECTION_VIEW = "fcs/application/eiaDirectionForm";

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getEiaDirectionForm_noUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(EiaDirectionController.class).getEiaDirectionForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getGasInjectionFormArguments")
  void getGasInjectionForm_validUser(EiaDirectionForm eiaDirectionForm, RestSearchItem restSearchItem) throws Exception {
    when(eiaDirectionService.getEiaDirectionForm(applicationVersion)).thenReturn(eiaDirectionForm);
    when(eiaDirectionFormService.getPrefilledEiaDirectionRef(any())).thenReturn(restSearchItem);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(EiaDirectionController.class).getEiaDirectionForm(APPLICATION_ID)))
            .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(EIA_DIRECTION_VIEW))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("prefilledEiaDirectionRef", restSearchItem)
        .containsEntry("cancelUrl", TASK_LIST_URL);
    assertThat((EiaDirectionForm) model.get("form"))
        .isEqualTo(eiaDirectionForm);
  }

  private static Stream<Arguments> getGasInjectionFormArguments() {
    var restSearchItem = new RestSearchItem(String.valueOf(SAT_ID_1), SAT_REF_1);
    return Stream.of(
        Arguments.of(new EiaDirectionForm(), EMPTY_REST_SEARCH_ITEM),
        Arguments.of(EiaDirectionTestUtil.getEiaDirectionFormWithSat(SAT_ID_1), restSearchItem),
        Arguments.of(EiaDirectionTestUtil.getEiaDirectionFormWithSatToSubmit(), EMPTY_REST_SEARCH_ITEM),
        Arguments.of(EiaDirectionTestUtil.getEiaDirectionFormWithNoSatToSubmit(), EMPTY_REST_SEARCH_ITEM),
        Arguments.of(EiaDirectionTestUtil.getEiaDirectionFormWithAllDataSet(SAT_ID_1) , restSearchItem)
    );
  }

  @SecurityTest
  void saveEiaDirectionForm_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(EiaDirectionController.class)
            .saveEiaDirectionForm(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveEiaDirectionForm_emptyForm() throws Exception {
    doCallRealMethod().when(formValidator).validate(any(), any());
    when(eiaDirectionFormService.getPrefilledEiaDirectionRef(any())).thenReturn(EMPTY_REST_SEARCH_ITEM);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(EiaDirectionController.class)
            .saveEiaDirectionForm(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(EIA_DIRECTION_VIEW))
        .andReturn().getModelAndView();

    verifyNoInteractions(eiaDirectionService);

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("prefilledEiaDirectionRef", EMPTY_REST_SEARCH_ITEM)
        .containsEntry("cancelUrl", TASK_LIST_URL);
    assertThat((EiaDirectionForm) model.get("form"))
        .usingRecursiveComparison()
        .isEqualTo(new EiaDirectionForm());
  }

  @Test
  void saveEiaDirectionForm_validForm() throws Exception {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSat(SAT_ID_1);
    mockMvc.perform(post(ReverseRouter.route(on(EiaDirectionController.class)
            .saveEiaDirectionForm(APPLICATION_ID, null, null)))
            .param("haveSubmittedEiaDirection", eiaDirectionForm.getHaveSubmittedEiaDirection().toString())
            .param("satId", String.valueOf(eiaDirectionForm.getSatId()))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + TASK_LIST_URL));

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    ArgumentCaptor<EiaDirectionForm> eiaDirectionFormArgumentCaptor = ArgumentCaptor.forClass(EiaDirectionForm.class);
    verify(eiaDirectionService, times(1))
        .saveEiaDirection(applicationVersionArgumentCaptor.capture(), eiaDirectionFormArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue()).isEqualTo(applicationVersion);
    assertThat(eiaDirectionFormArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(eiaDirectionForm);
  }
}
