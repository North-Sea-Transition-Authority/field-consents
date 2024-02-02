package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentDataController.class)
class ConsentDataControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/consent/data/consentDataForm";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsentDataService consentDataService;

  @MockBean
  private ConsentDataFormValidator validator;

  @Captor
  private ArgumentCaptor<ConsentDataForm> consentDataFormCaptor;

  private Application application;
  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getConsentDataAndRedirect_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
            .getConsentDataAndRedirect(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getConsentDataAndRedirect_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
            .getConsentDataAndRedirect(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void editConsentData_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editConsentData_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitConsentData_notSignedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
            .submitConsentData(APPLICATION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitConsentData_doesNotHavePermission() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
            .submitConsentData(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void getConsentDataAndRedirect_consentDataExists() throws Exception {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.of(consentData));

    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
        .getConsentDataAndRedirect(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID))));
  }

  @Test
  void getConsentDataAndRedirect_consentDataDoesNotExist() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
            .getConsentDataAndRedirect(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentDataController.class).editConsentData(APPLICATION_ID))));
  }

  @Test
  void editConsentData() throws Exception {
    var form = ConsentDataForm.from(LocalDate.parse("2024-01-01"), LocalDate.parse("2025-01-01"));

    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consentDataService.getPrefilledConsentDataForm(application)).thenReturn(form);

    var model = mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class).editConsentData(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", "Edit consent data"))
        .andExpect(model().attributeExists("form"))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model.get("form")).isEqualTo(form);
  }

  @Test
  void submitConsentData() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);

    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
        .submitConsentData(APPLICATION_ID, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("consentStartDate.year", "2024")
        .param("consentStartDate.month", "1")
        .param("consentStartDate.day", "1")
        .param("consentEndDate.year", "2025")
        .param("consentEndDate.month", "1")
        .param("consentEndDate.day", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(APPLICATION_ID))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Consent data saved")
            .build()));

    var expectedConsentStartDate = LocalDate.parse("2024-01-01");
    var expectedConsentEndDate = LocalDate.parse("2025-01-01");

    verify(validator).validate(consentDataFormCaptor.capture(), any(BindingResult.class));

    var form = consentDataFormCaptor.getValue();

    assertThat(form)
        .extracting(
            consentDataForm -> consentDataForm.consentStartDate().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.consentEndDate().getAsLocalDate().orElseThrow()
        ).containsExactly(
            expectedConsentStartDate,
            expectedConsentEndDate
        );

    verify(consentDataService).saveConsentData(application, form);
  }

  @Test
  void submitConsentData_validationError() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("consentStartDate.yearInput.inputValue", "errorCode", "defaultMessage");
      return null;
    })
        .when(validator)
        .validate(any(ConsentDataForm.class), any(BindingResult.class));

    var model = mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
            .submitConsentData(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("consentStartDate.year", "2024")
            .param("consentStartDate.month", "1")
            .param("consentStartDate.day", "1")
            .param("consentEndDate.year", "2025")
            .param("consentEndDate.month", "1")
            .param("consentEndDate.day", "1"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attributeExists("form"))
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model.get("form"))
        .asInstanceOf(type(ConsentDataForm.class))
        .extracting(
            form -> form.consentStartDate().getAsLocalDate().orElseThrow(),
            form -> form.consentEndDate().getAsLocalDate().orElseThrow()
        ).containsExactly(
            LocalDate.parse("2024-01-01"),
            LocalDate.parse("2025-01-01")
        );

    verify(consentDataService, never()).saveConsentData(any(), any());
  }
}
