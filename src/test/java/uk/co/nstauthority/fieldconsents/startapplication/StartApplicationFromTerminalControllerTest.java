package uk.co.nstauthority.fieldconsents.startapplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationRedirectService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import util.StreamUtils;

@ContextConfiguration(classes = StartApplicationFromTerminalController.class)
class StartApplicationFromTerminalControllerTest extends AbstractControllerTest {

  private static final Integer TERMINAL_ID = 1;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private StartApplicationControllerHelperService startApplicationControllerHelperService;

  @MockBean
  private ApplicationRedirectService applicationRedirectService;

  @MockBean
  private StartApplicationFormValidator formValidator;

  private Map<String, String> applicationTypeMap;

  private ApplicationVersion applicationVersion;

  private StartApplicationForm form;
  
  @BeforeEach
  void setUp() {
    applicationTypeMap = Arrays.stream(ConsentLengthType.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ConsentLengthType::getDisplayName));

    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(startApplicationControllerHelperService.getApplicationTypesMap(AssetType.TERMINAL)).thenReturn(applicationTypeMap);
    form = new StartApplicationForm(ApplicationType.PRODUCTION);
  }

  @Test
  @WithMockUser
  void getStartApplicationModelAndView() throws Exception {
    String createApplicationUrl = ReverseRouter.route(on(StartApplicationFromTerminalController.class)
        .createNewApplicationOfType(
            TERMINAL_ID,
            null,
            ReverseRouter.emptyBindingResult())
    );
    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
                .getStartApplicationModelAndView(TERMINAL_ID)))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/startApplication"))
            .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(applicationTypeMap, model.get("applicationTypes"));
    assertEquals(createApplicationUrl, model.get("createApplicationUrl"));
  }

  @Test
  void createNewApplicationOfType_notAuthorized() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .getStartApplicationModelAndView(TERMINAL_ID)))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void createNewApplicationOfType() throws Exception {
    doCallRealMethod().when(applicationRedirectService).getTaskListModelAndViewByApplicationType(form.getApplicationType());

    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .createNewApplicationOfType(TERMINAL_ID, form, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void createNewApplicationOfType_whenUnauthorized() throws Exception {
    doCallRealMethod().when(applicationRedirectService).getTaskListModelAndViewByApplicationType(form.getApplicationType());

    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .createNewApplicationOfType(TERMINAL_ID, form, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}