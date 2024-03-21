package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ContextConfiguration(classes = ConsentDataController.class)
class ConsentDataControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/consent/data/consentDataForm";

  @MockBean
  private ConsentDataService consentDataService;

  @MockBean
  private ConsentDataFormValidator consentDataFormValidator;

  @MockBean
  private ConsentFigureUnitService consentFigureUnitService;

  @MockBean
  private ConsentLengthService consentLengthService;

  @MockBean
  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

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
  void editConsentData_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class)
            .editConsentData(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editConsentData_userDoesNotHaveEditConsentDataCaseProcessingActionItem() throws Exception {
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
  void submitConsentData_userDoesNotHaveEditConsentDataCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
            .submitConsentData(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void editConsentData() throws Exception {
    var consentLengthDetails = new ConsentLengthDetails();
    var consentLengthType = ConsentLengthType.SHORT_TERM;
    consentLengthDetails.setConsentLength(consentLengthType);

    var form = ConsentDataForm.fromShortTermOrAnnualProductionApplication(ConsentDataTestUtil.newBuilder().build());

    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(ProductionUnit.KSCM_PER_DAY);

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DATA));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentDataService.getPrefilledConsentDataForm(applicationVersion, consentLengthDetails)).thenReturn(form);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentDataController.class).editConsentData(APPLICATION_ID)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", "Edit consent data"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("applicationType", application.getType()))
        .andExpect(model().attribute("consentLengthType", consentLengthType))
        .andExpect(model().attribute("consentFigureUnitView", consentFigureUnitView));
  }

  @SecurityTest
  void submitConsentData_consentDataDoesNotExist() throws Exception {
    var consentLengthDetails = new ConsentLengthDetails();
    var consentLengthType = ConsentLengthType.SHORT_TERM;
    consentLengthDetails.setConsentLength(consentLengthType);

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DATA));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
        .submitConsentData(APPLICATION_ID, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewConsentPreparationPage(APPLICATION_ID, null))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Consent data saved")
            .build()));

    verify(consentDataFormValidator)
        .validate(consentDataFormCaptor.capture(), eq(application), eq(consentLengthType), any(BindingResult.class));

    var form = consentDataFormCaptor.getValue();

    verify(applicationDocumentInstanceService).createDocumentInstancesForApplication(application);
    verify(consentDataService).saveConsentData(application, consentLengthType, form);
  }

  @SecurityTest
  void submitConsentData_consentDataDoesExist() throws Exception {
    var consentLengthDetails = new ConsentLengthDetails();
    var consentLengthType = ConsentLengthType.SHORT_TERM;
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DATA));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.of(consentData));

    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
            .submitConsentData(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewConsentPreparationPage(APPLICATION_ID, null))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Consent data saved")
            .build()));

    verify(consentDataFormValidator)
        .validate(consentDataFormCaptor.capture(), eq(application), eq(consentLengthType), any(BindingResult.class));

    var form = consentDataFormCaptor.getValue();

    verify(applicationDocumentInstanceService, never()).createDocumentInstancesForApplication(any());
    verify(consentDataService).saveConsentData(application, consentLengthType, form);
  }

  @SecurityTest
  void submitConsentData_validationError() throws Exception {
    var consentLengthDetails = new ConsentLengthDetails();
    var consentLengthType = ConsentLengthType.SHORT_TERM;
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(ProductionUnit.KSCM_PER_DAY);

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DATA));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(3, BindingResult.class);
      bindingResult.rejectValue("consentStartDateInput.yearInput.inputValue", "errorCode", "defaultMessage");
      return null;
    })
        .when(consentDataFormValidator)
        .validate(any(ConsentDataForm.class), eq(application), eq(consentLengthType), any(BindingResult.class));

    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);

    mockMvc.perform(post(ReverseRouter.route(on(ConsentDataController.class)
            .submitConsentData(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", "Edit consent data"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("applicationType", application.getType()))
        .andExpect(model().attribute("consentLengthType", consentLengthType))
        .andExpect(model().attribute("consentFigureUnitView", consentFigureUnitView));

    verify(applicationDocumentInstanceService, never()).createDocumentInstancesForApplication(any());
    verify(consentDataService, never()).saveConsentData(any(), any(), any());
  }
}
