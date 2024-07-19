package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.EXISTING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_UPLOAD_COMPONENT_ATTRIBUTES;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.EiaRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.HabitatsRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ConsultationResponseController.class)
class ConsultationResponseControllerTest extends AbstractApplicationControllerTest {

  private static final int CONSULTATION_ID = 1;
  private static final String APPLICATION_REFERENCE = "APPLICATION_REFERENCE";
  private static final String PAGE_TITLE = "Consultation response";
  private static final String VIEW_NAME = "fcs/application/consultation/responseForm";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ConsultationResponseFormValidator validator;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Consultation consultation;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);
    consultation.setRequestDeadline(Instant.now().plus(5, ChronoUnit.DAYS));
    consultation.setResponderWuaId(user.wuaId());

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getResponseForm_securityTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsultationResponseController.class).getResponseForm(APPLICATION_ID,
            user))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getResponseForm_requiresEiaDecision() throws Exception {
    mockGetResponseFormInvocations(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsultationResponseController.class)
            .getResponseForm(APPLICATION_ID, user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView.getModel())
        .containsAllEntriesOf(getBaseModelAndView())
        .containsKey("form")
        .containsEntry("habitatsRegsRadioOptions", EnumSet.allOf(HabitatsRegsResponseType.class))
        .containsEntry("eiaRegsRadioOptions", EnumSet.allOf(EiaRegsResponseType.class));
  }

  @Test
  void getResponseForm_doesNotRequireEiaDecision() throws Exception {
    mockGetResponseFormInvocations(false);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsultationResponseController.class)
            .getResponseForm(APPLICATION_ID, user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView.getModel())
        .containsAllEntriesOf(getBaseModelAndView())
        .containsKey("form")
        .containsEntry("habitatsRegsRadioOptions", EnumSet.allOf(HabitatsRegsResponseType.class))
        .doesNotContainKey("eiaRegsRadioOptions");
  }

  @Test
  void submitResponseForm() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consultationService.getLatestOpenConsultation(application)).thenReturn(consultation);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    var habitatsRegsDescription = "habitatsRegsDescription";
    var eiaRegsDescription = "eiaRegsDescription";

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
            .submitResponseForm(APPLICATION_ID, null, null, null, null)))
            .param("habitatsRegsResponseType", HabitatsRegsResponseType.AGREE.name())
            .param("habitatsRegsAgreeDescription.inputValue", habitatsRegsDescription)
            .param("eiaRegsResponseType", EiaRegsResponseType.AGREE.name())
            .param("eiaRegsAgreeDescription.inputValue", eiaRegsDescription)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Consultation response submitted for application %s".formatted(APPLICATION_REFERENCE))
            .build()))
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))));

    verify(validator).validate(any(ConsultationResponseForm.class), any(BindingResult.class), eq(applicationVersion));
    verify(consultationService).saveConsultationResponse(
        applicationVersion,
        consultation,
        user,
        HabitatsRegsResponseType.AGREE,
        habitatsRegsDescription,
        EiaRegsResponseType.AGREE,
        eiaRegsDescription,
        Collections.emptyList()
    );
  }

  @Test
  void submitResponseForm_withoutDescriptions() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consultationService.getLatestOpenConsultation(application)).thenReturn(consultation);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
            .submitResponseForm(APPLICATION_ID, null, null, null, null)))
            .param("habitatsRegsResponseType", HabitatsRegsResponseType.AGREE.name())
            .param("eiaRegsResponseType", EiaRegsResponseType.AGREE.name())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Consultation response submitted for application %s".formatted(APPLICATION_REFERENCE))
            .build()))
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))));

    verify(validator).validate(any(ConsultationResponseForm.class), any(BindingResult.class), eq(applicationVersion));
    verify(consultationService).saveConsultationResponse(
        applicationVersion,
        consultation,
        user,
        HabitatsRegsResponseType.AGREE,
        null,
        EiaRegsResponseType.AGREE,
        null,
        Collections.emptyList()
    );
  }

  @Test
  void submitResponseForm_withValidationErrors() throws Exception {
    mockGetResponseFormInvocations(false);

    doAnswer(invocation -> {
      var bindingResult = invocation.getArgument(1, BindingResult.class);
      bindingResult.rejectValue("habitatsRegsResponseType", "invalid", "this radio option is invalid");
      return null;
    }).when(validator).validate(any(ConsultationResponseForm.class), any(BindingResult.class), eq(applicationVersion));

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
            .submitResponseForm(APPLICATION_ID, null, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(consultationService, never()).saveConsultationResponse(any(), any(), any(), any(), any(), any(), any(), any());
  }

  private Map<String, Object> getBaseModelAndView() {
    return Map.of(
        "pageTitle", PAGE_TITLE,
        "fileUploadAttributes", FILE_UPLOAD_COMPONENT_ATTRIBUTES,
        "applicationReference", APPLICATION_REFERENCE,
        "consultationSummaryView", ConsultationRequestView.from(consultation),
        "backLinkUrl", ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))
    );
  }
  private void mockGetResponseFormInvocations(boolean requiresEiaRegsResponse) {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    when(consultationService.getLatestOpenConsultation(application)).thenReturn(consultation);
    when(consultationService.requiresEiaRegsResponse(applicationVersion)).thenReturn(requiresEiaRegsResponse);
    when(fileControllerHelperService.fileUploadComponentAttributes(eq(EXISTING_DOCUMENTS), eq(ConsultationResponseFileController.class), any(), any())).thenReturn(FILE_UPLOAD_COMPONENT_ATTRIBUTES);

    doAnswer(invocation -> {
      addApplicationSummaryAttributes(invocation.getArgument(1, ModelAndView.class));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class), eq(user));
  }

  private void addApplicationSummaryAttributes(ModelAndView modelAndView) {
    modelAndView
        .addObject("summarySections", Collections.emptyList())
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", false);
  }

}
