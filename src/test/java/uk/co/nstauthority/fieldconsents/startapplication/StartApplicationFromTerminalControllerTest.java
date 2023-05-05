package uk.co.nstauthority.fieldconsents.startapplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@ContextConfiguration(classes = StartApplicationFromTerminalController.class)
class StartApplicationFromTerminalControllerTest extends AbstractControllerTest {

  private static final Integer TERMINAL_ID = 1;

  private static final String MANAGE_TERMINAL_URL_BASE = "/manage-asset/facilities/1/";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private StartApplicationControllerHelperService startApplicationControllerHelperService;

  @MockBean
  private StartApplicationFormValidator formValidator;

  @MockBean
  private StartApplicationOperatorFormValidator operatorFormValidator;

  @MockBean
  private TerminalService terminalService;

  private Map<String, String> applicationTypeMap;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationTypeMap = Arrays.stream(ConsentLengthType.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ConsentLengthType::getDisplayName));

    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(startApplicationControllerHelperService.getApplicationTypesMap(AssetType.TERMINAL)).thenReturn(applicationTypeMap);
  }


  @Test
  @WithMockUser
  void getStartApplicationForm() throws Exception {
    String continueStartApplicationUrl = ReverseRouter.route(on(StartApplicationFromTerminalController.class)
        .continueStartApplicationOfType(
            TERMINAL_ID,
            null,
            ReverseRouter.emptyBindingResult(),
            null)
    );
    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
                .getStartApplicationForm(TERMINAL_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/startApplication"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(applicationTypeMap, model.get("applicationTypes"));
    assertEquals(continueStartApplicationUrl, model.get("continueStartApplicationUrl"));
  }

  @Test
  void getStartApplicationForm_notAuthorized() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .getStartApplicationForm(TERMINAL_ID)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void continueStartApplicationOfType() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .continueStartApplicationOfType(TERMINAL_ID, null, ReverseRouter.emptyBindingResult(), null)))
            .param("applicationType", ApplicationType.FLARE.name())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeCount(1))
        .andExpect(flash().attribute("applicationType", ApplicationType.FLARE))
        .andExpect(view().name("redirect:" + MANAGE_TERMINAL_URL_BASE + "start-application/operator"));
  }

  @Test
  @WithMockUser
  void continueStartApplicationOfType_formErrors() throws Exception {
    doCallRealMethod().when(formValidator).validate(any(), any());

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
                .continueStartApplicationOfType(TERMINAL_ID, null, ReverseRouter.emptyBindingResult(), null)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/startApplication"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(applicationTypeMap, model.get("applicationTypes"));
    assertEquals(MANAGE_TERMINAL_URL_BASE + "start-application", model.get("continueStartApplicationUrl"));
  }

  @Test
  void continueStartApplicationOfType_whenUnauthorized() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .continueStartApplicationOfType(TERMINAL_ID, null, ReverseRouter.emptyBindingResult(), null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void getStartApplicationOperatorForm() throws Exception {
    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
                .getStartApplicationOperatorForm(TERMINAL_ID, null)))
                .flashAttr("applicationType", ApplicationType.FLARE)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/operatorForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("createApplicationUrl", MANAGE_TERMINAL_URL_BASE + "start-application/operator"),
            entry("cancelUrl", MANAGE_TERMINAL_URL_BASE)
        );
    var form = (StartApplicationOperatorForm) model.get("form");
    assertThat(form.getApplicationType()).isEqualTo(ApplicationType.FLARE);
    assertThat(form.getOrganisationUnitId().getInputValue()).isNull();
  }

  @Test
  void getStartApplicationOperatorForm_notAuthorized() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .getStartApplicationOperatorForm(TERMINAL_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void createNewApplication() throws Exception {
    OrganisationUnitJson operatorOuJson = new OrganisationUnitJson(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1,
        ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1);
    when(organisationUnitService.getOrganisationUnitById(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1,
        "Lookup organisation unit prior to creating a terminal application"))
        .thenReturn(operatorOuJson);
    when(terminalService.getTerminalWithOperator(TERMINAL_ID, "Lookup terminal prior to creating a terminal application"))
        .thenReturn(terminal1JsonWithOperator);
    when(applicationService.createNewApplicationForTerminal(ApplicationType.FLARE, terminal1JsonWithOperator, operatorOuJson))
        .thenReturn(applicationVersion);

    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .createNewApplication(TERMINAL_ID, null, ReverseRouter.emptyBindingResult())))
            .param("applicationType", ApplicationType.FLARE.name())
            .param("organisationUnitId.inputValue", String.valueOf(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  @WithMockUser
  void createNewApplication_formErrors() throws Exception {
    doCallRealMethod().when(operatorFormValidator).validate(any(), any());

    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
                .createNewApplication(TERMINAL_ID, null, ReverseRouter.emptyBindingResult())))
                .param("applicationType", ApplicationType.FLARE.name())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/operatorForm"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .contains(
            entry("createApplicationUrl", MANAGE_TERMINAL_URL_BASE + "start-application/operator"),
            entry("cancelUrl", MANAGE_TERMINAL_URL_BASE)
        );
    var form = (StartApplicationOperatorForm) model.get("form");
    assertThat(form.getApplicationType()).isEqualTo(ApplicationType.FLARE);
    assertThat(form.getOrganisationUnitId().getInputValue()).isNull();
  }

  @Test
  void createNewApplication_whenUnauthorized() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .createNewApplication(TERMINAL_ID, null, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

}