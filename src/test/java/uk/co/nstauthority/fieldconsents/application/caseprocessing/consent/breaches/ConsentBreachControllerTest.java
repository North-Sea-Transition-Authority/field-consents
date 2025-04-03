package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.BREACH_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_BREACH_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.RECORD_BREACH;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REMOVE_BREACH;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentBreachController.class)
class ConsentBreachControllerTest extends AbstractApplicationControllerTest {

  @MockitoBean
  private ApplicationService applicationService;

  @MockitoBean
  private ConsentService consentService;

  @MockitoBean
  private ConsentBreachSummaryService consentBreachSummaryService;

  @MockitoBean
  private ConsentBreachFormValidator consentBreachFormValidator;

  @MockitoBean
  private ConsentBreachService consentBreachService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {

    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

  }

  @SecurityTest
  void breachInformation_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .breachInformation(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void breachInformationAction() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .breachInformation(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
    verify(caseProcessingActionService)
        .userHasAnyAction(applicationVersion, user, BREACH_INFORMATION);
  }

  @SecurityTest
  void getConsentBreachForm_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getConsentBreachForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getConsentBreachForm_userDoesNotHaveBreachFormInformationAction() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getConsentBreachForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
    verify(caseProcessingActionService)
        .userHasAnyAction(applicationVersion, user, RECORD_BREACH, EDIT_BREACH_INFORMATION);
  }

  @SecurityTest
  void saveConsentBreach_noUser() throws Exception {
    var consentBreachForm = new ConsentBreachForm();
    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .saveConsentBreach(APPLICATION_ID, consentBreachForm, null, user, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveConsentBreach_userDoesNotHaveBreachInformationAction() throws Exception {
    var consentBreachForm = new ConsentBreachForm();
    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .saveConsentBreach(APPLICATION_ID, consentBreachForm, null, user, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
    verify(caseProcessingActionService, times(1))
        .userHasAnyAction(applicationVersion, user, RECORD_BREACH, EDIT_BREACH_INFORMATION);
  }

  @SecurityTest
  void getDeleteBreachConfirmation_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getDeleteBreachConfirmation(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getDeleteBreachConfirmation_userDoesNotHaveBreachInformationAction() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getDeleteBreachConfirmation(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
    verify(caseProcessingActionService, times(1))
        .userHasAnyAction(applicationVersion, user, REMOVE_BREACH);
  }

  @SecurityTest
  void deleteBreach_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .deleteBreach(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void deleteBreach_userDoesNotHaveBreachInformationAction() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .deleteBreach(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
    verify(caseProcessingActionService, times(1))
        .userHasAnyAction(applicationVersion, user, REMOVE_BREACH);
  }

  @Test
  void breachInformation() throws Exception {
    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    int consentBreachId = 1;
    var consentBreach = new ConsentBreach(consentBreachId);
    var consentBreachView = new ConsentBreachView(
        "2",
        "test wua",
        "today");
    var captionTitle = "Caption Title";
    var breachInformationGroupActions = List.of(CaseProcessingActionView
        .from(CaseProcessingActionItem.BREACH_INFORMATION,
            applicationVersion));
    var breachInformationCardGroupActions = List.of(CaseProcessingActionView
        .from(CaseProcessingActionItem.BREACH_INFORMATION,
            applicationVersion));

    when(applicationVersionService.getApplicationVersionById(applicationId))
        .thenReturn(applicationVersion);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(captionTitle);
    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.of(consentBreach));
    when(consentBreachSummaryService.getConsentBreachView(consentBreach))
        .thenReturn(consentBreachView);
    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user,
        CaseProcessingActionGroup.BREACH_INFORMATION))
        .thenReturn(breachInformationGroupActions);
    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user,
        CaseProcessingActionGroup.BREACH_INFORMATION_CARD))
        .thenReturn(breachInformationCardGroupActions);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .breachInformation(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("captionTitle", captionTitle))
        .andExpect(model().attribute("consentBreachView", consentBreachView))
        .andExpect(model().attribute("breachInformationGroupActions", breachInformationGroupActions))
        .andExpect(model().attribute("breachInformationCardGroupActions", breachInformationCardGroupActions))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(applicationId, null, null, null))));
  }

  @Test
  void getConsentBreachForm_newConsentBreach() throws Exception {
    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    var captionTitle = "Caption Title";

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId))
        .thenReturn(applicationVersion);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(captionTitle);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getConsentBreachForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("captionTitle", captionTitle))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentBreachController.class)
                .breachInformation(applicationId, null))))
        .andReturn()
        .getModelAndView();

    assertThat((ConsentBreachForm) modelAndView.getModel().get("form"))
        .extracting(consentBreachForm -> consentBreachForm
            .getConsentBreachText()
            .getInputValue())
        .isNull();
  }

  @Test
  void getConsentBreachForm_existingConsentBreach() throws Exception {
    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    var captionTitle = "Caption Title";

    var consentBreach = new ConsentBreach(2);
    var breachText = "test breach text";
    consentBreach.setBreachText(breachText);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId))
        .thenReturn(applicationVersion);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(captionTitle);
    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.of(consentBreach));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getConsentBreachForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("captionTitle", captionTitle))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentBreachController.class)
                .breachInformation(applicationId, null))))
        .andReturn()
        .getModelAndView();

    assertThat((ConsentBreachForm) modelAndView.getModel().get("form"))
        .extracting(consentBreachForm -> consentBreachForm
            .getConsentBreachText()
            .getInputValue())
        .isEqualTo(breachText);
  }

  @Test
  void saveConsentBreach_newConsentBreach_validForm() throws Exception {
    var application = applicationVersion.getApplication();
    var consent = new Consent();
    var breachText = "test breach text";

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Breach recorded")
        .build();

    when(applicationService.getApplicationById(APPLICATION_ID))
        .thenReturn(application);
    when(consentService.getConsent(application))
        .thenReturn(consent);

    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .saveConsentBreach(APPLICATION_ID, null, null, user, null)))
            .with(user(user))
            .with(csrf())
            .param("consentBreachText", breachText))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(consentBreachService)
        .saveConsentBreach(consent, breachText, user);
  }

  @Test
  void saveConsentBreach_existingConsentBreach_validForm() throws Exception {
    var application = applicationVersion.getApplication();
    var consent = new Consent();
    var consentBreach = new ConsentBreach(2);
    var breachText = "test breach text";

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Breach recorded")
        .build();

    when(applicationService.getApplicationById(APPLICATION_ID))
        .thenReturn(application);
    when(consentService.getConsent(application))
        .thenReturn(consent);

    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.of(consentBreach));

    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .saveConsentBreach(APPLICATION_ID, null, null, user, null)))
            .with(user(user))
            .with(csrf())
            .param("consentBreachText", breachText))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(consentBreachService, times(1))
        .saveConsentBreach(consent, breachText, user);
  }

  @Test
  void saveConsentBreach_newConsentBreach_invalidForm() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("consentBreachText", "required", "information about the breach");
      return null;
    })
        .when(consentBreachFormValidator)
        .validate(any(ConsentBreachForm.class), any(BindingResult.class));

    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    var consentBreachForm = new ConsentBreachForm();
    var captionTitle = "Caption Title";

    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.empty());
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(captionTitle);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .saveConsentBreach(APPLICATION_ID, consentBreachForm, null, user, null)))
            .with(user(user))
            .with(csrf())
            .param("consentBreachText", ""))
        .andExpect(status().isOk())
        .andExpect(model().attribute("captionTitle",
            captionTitle))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentBreachController.class)
                .breachInformation(applicationId, null))))
        .andReturn()
        .getModelAndView();

    assertThat((ConsentBreachForm) modelAndView.getModel().get("form"))
        .extracting(returnedConsentBreachForm -> returnedConsentBreachForm
            .getConsentBreachText()
            .getInputValue())
        .isEqualTo(consentBreachForm.getConsentBreachText().getInputValue());

    verify(consentBreachService, never())
        .saveConsentBreach(any(), any(), any());
  }

  @Test
  void saveConsentBreach_existingConsentBreach_invalidForm() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("consentBreachText", "required", "information about the breach");
      return null;
    })
        .when(consentBreachFormValidator)
        .validate(any(ConsentBreachForm.class), any(BindingResult.class));

    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    var consentBreach = new ConsentBreach(2);
    var consentBreachForm = new ConsentBreachForm();
    var captionTitle = "Caption Title";

    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.of(consentBreach));
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(captionTitle);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .saveConsentBreach(APPLICATION_ID, consentBreachForm, null, user, null)))
            .with(user(user))
            .with(csrf())
            .param("consentBreachText", ""))
        .andExpect(status().isOk())
        .andExpect(model().attribute("captionTitle",
            captionTitle))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsentBreachController.class)
                .breachInformation(applicationId, null))))
        .andReturn()
        .getModelAndView();

    assertThat((ConsentBreachForm) modelAndView.getModel().get("form"))
        .extracting(returnedConsentBreachForm -> returnedConsentBreachForm
            .getConsentBreachText()
            .getInputValue())
        .isEqualTo(consentBreachForm.getConsentBreachText().getInputValue());

    verify(consentBreachService, never())
        .saveConsentBreach(any(), any(), any());
  }

  @Test
  void getDeleteBreachConfirmation() throws Exception {
    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    var captionTitle = "Caption Title";
    var consentBreach = new ConsentBreach();
    var breachText = "test breach text";
    var addedByUser = "test user";
    var addedDateTime = "sometime";
    var consentBreachView = new ConsentBreachView(breachText, addedByUser, addedDateTime);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId))
        .thenReturn(applicationVersion);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(captionTitle);
    when(consentBreachService.getConsentBreachByApplication(application))
        .thenReturn(consentBreach);
    when(consentBreachSummaryService.getConsentBreachView(consentBreach))
        .thenReturn(consentBreachView);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentBreachController.class)
            .getDeleteBreachConfirmation(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("consentBreachView", consentBreachView))
        .andExpect(model().attribute("captionTitle", captionTitle))
        .andExpect(model().attribute("breachInformationCardGroupActions", Collections.emptyList()))
        .andExpect(model().attribute("cancelUrl",
            ReverseRouter.route(on(ConsentBreachController.class)
                .breachInformation(applicationId, null))));
  }

  @Test
  void deleteBreach() throws Exception {
    var application = applicationVersion.getApplication();
    var consentBreach = new ConsentBreach(2);
    var consentBreachForm = new ConsentBreachForm();
    var breachText = "test breach text";
    consentBreachForm.setConsentBreachText(breachText);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Breach removed")
        .build();

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(consentBreachService.getConsentBreachByApplication(application))
        .thenReturn(consentBreach);

    mockMvc.perform(post(ReverseRouter.route(on(ConsentBreachController.class)
            .deleteBreach(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(consentBreachService, times(1))
        .deleteConsentBreach(consentBreach);
  }
}
