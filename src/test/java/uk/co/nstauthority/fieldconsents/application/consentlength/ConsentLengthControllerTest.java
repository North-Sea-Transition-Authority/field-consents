package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentLengthController.class)
class ConsentLengthControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsentLengthService consentLengthService;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ConsentLengthFormValidator consentLengthFormValidator;

  @MockBean
  private ConsentLengthControllerHelperService consentLengthHelperService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthForm consentLengthForm;

  private Map<String, String> consentTypeMap;

  private Map<String, String> annualConsentMap;

  private Map<String, String> longTermConsentMap;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationService.getApplicationById(ApplicationTestUtil.APPLICATION_ID)).thenReturn(applicationVersion.getApplication());
    when(applicationVersionService.getApplicationVersionById(ApplicationTestUtil.APPLICATION_ID)).thenReturn(applicationVersion);

    consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthForm();

    longTermConsentMap = ConsentLengthTestUtil.getLongTermConsentYearsMap();
    when(consentLengthHelperService.getLongTermConsentYearsMap()).thenReturn(longTermConsentMap);
    annualConsentMap = ConsentLengthTestUtil.getAnnualConsentYearsMap();
    when(consentLengthHelperService.getAnnualConsentYearsMap()).thenReturn(annualConsentMap);
    consentTypeMap = ConsentLengthTestUtil.getConsentLengthTypeMap();
    when(consentLengthHelperService.getConsentTypesMap(applicationVersion.getApplication())).thenReturn(consentTypeMap);
  }

  @Test
  @WithMockUser
  void getConsentLengthForm() throws Exception {
    when(consentLengthService.getConsentLengthForm(applicationVersion)).thenReturn(consentLengthForm);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsentLengthController.class)
            .getConsentLengthForm(APPLICATION_ID)))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consentLengthForm"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertEquals(consentTypeMap, model.get("consentTypes"));
    assertEquals(annualConsentMap, model.get("annualConsentYears"));
    assertEquals(longTermConsentMap, model.get("longTermStartYears"));
  }

  @Test
  void getConsentLengthForm_withUnauthorizedUser() throws Exception {
    when(consentLengthService.getConsentLengthForm(applicationVersion)).thenReturn(consentLengthForm);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(APPLICATION_ID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void saveConsentLengthDetails() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentLengthController.class)
            .saveConsentLengthDetails(APPLICATION_ID, consentLengthForm, ReverseRouter.emptyBindingResult())))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list/"));
  }

  @Test
  void saveConsentLengthDetails_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(
            on(ConsentLengthController.class).saveConsentLengthDetails(APPLICATION_ID, consentLengthForm,
                ReverseRouter.emptyBindingResult())))
        .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}