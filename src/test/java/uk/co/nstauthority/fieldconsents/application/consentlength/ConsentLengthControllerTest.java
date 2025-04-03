package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentLengthController.class)
class ConsentLengthControllerTest extends AbstractApplicationControllerTest {

  @MockitoBean
  private ConsentLengthService consentLengthService;

  @MockitoBean
  private ConsentLengthFormValidator consentLengthFormValidator;

  @MockitoBean
  private ConsentLengthControllerHelperService consentLengthHelperService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthForm consentLengthForm;

  private Map<String, String> consentTypeMap;

  private Map<String, String> annualConsentMap;

  private Map<String, String> longTermConsentMap;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    consentLengthForm = ConsentLengthTestUtil.getAnnualConsentLengthForm();

    longTermConsentMap = ConsentLengthTestUtil.getLongTermConsentYearsMap();
    when(consentLengthHelperService.getLongTermConsentYearsMap()).thenReturn(longTermConsentMap);
    annualConsentMap = ConsentLengthTestUtil.getAnnualConsentYearsMap();
    when(consentLengthHelperService.getAnnualConsentYearsMap()).thenReturn(annualConsentMap);
    consentTypeMap = ConsentLengthTestUtil.getConsentLengthTypeMap();
    when(consentLengthHelperService.getConsentTypesMap(applicationVersion.getApplication())).thenReturn(consentTypeMap);
  }

  @Test
  void getConsentLengthForm_applicationIsNotRevision() throws Exception {
    when(consentLengthService.getConsentLengthForm(applicationVersion)).thenReturn(consentLengthForm);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsentLengthController.class)
            .getConsentLengthForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consentLengthForm"))
        .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModel())
        .containsEntry("applicationIsRevision", false)
        .doesNotContainKey("consentLengthView")
        .containsEntry("consentTypes", consentTypeMap)
        .containsEntry("annualConsentYears", annualConsentMap)
        .containsEntry("longTermStartYears", longTermConsentMap);
  }

  @Test
  void getConsentLengthForm_applicationIsRevision() throws Exception {
    applicationVersion.getApplication().setVariationNo(1);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2024, 8, 12));

    when(consentLengthService.getConsentLengthForm(applicationVersion)).thenReturn(consentLengthForm);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsentLengthController.class)
            .getConsentLengthForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consentLengthForm"))
        .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModel())
        .containsEntry("applicationIsRevision", true)
        .containsEntry("consentLengthView", ConsentLengthView.from(consentLengthDetails))
        .containsEntry("consentTypes", consentTypeMap)
        .containsEntry("annualConsentYears", annualConsentMap)
        .containsEntry("longTermStartYears", longTermConsentMap);
  }

  @SecurityTest
  void getConsentLengthForm_withUnauthorizedUser() throws Exception {
    when(consentLengthService.getConsentLengthForm(applicationVersion)).thenReturn(consentLengthForm);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void saveConsentLengthDetails() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentLengthController.class)
            .saveConsentLengthDetails(APPLICATION_ID, consentLengthForm, ReverseRouter.emptyBindingResult())))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/applications/1/task-list"));
  }

  @SecurityTest
  void saveConsentLengthDetails_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(
            on(ConsentLengthController.class).saveConsentLengthDetails(APPLICATION_ID, consentLengthForm,
                ReverseRouter.emptyBindingResult())))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}
