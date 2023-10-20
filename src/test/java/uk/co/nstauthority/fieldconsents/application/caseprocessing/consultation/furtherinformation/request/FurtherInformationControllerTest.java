package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.request;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = FurtherInformationRequestController.class)
class FurtherInformationRequestControllerTest extends AbstractApplicationControllerTest {

  private static final Class<FurtherInformationRequestController> CONTROLLER_CLASS = FurtherInformationRequestController.class;
  private static final String VIEW_NAME = "fcs/application/consultation/further-information/requestForm";
  private static final String PAGE_TITLE = "Request further information";
  private static final String REQUEST_TEXT = "request text";
  private static final int CONSULTATION_ID = 1;
  private static final String APPLICATION_REFERENCE = "reference";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private FurtherInformationService furtherInformationService;

  private ApplicationVersion applicationVersion;
  private Application application;
  private Consultation consultation;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();
    consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getFurtherInformationForLatestConsultation_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getFurtherInformationForLatestConsultation(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getFurtherInformationForLatestConsultation() throws Exception {
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(consultationService.getLatestOpenConsultation(application)).thenReturn(consultation);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .getFurtherInformationForLatestConsultation(APPLICATION_ID)))
        .with(user(user))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getFurtherInformationForConsultation(APPLICATION_ID, CONSULTATION_ID))));
  }

  @Test
  void getFurtherInformationForConsultation() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consultationService.getOpenConsultationByIdAndApplication(CONSULTATION_ID, application)).thenReturn(consultation);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    doAnswer(invocation -> {
      addSummarySectionsToModelAndView(invocation.getArgument(1, ModelAndView.class));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getFurtherInformationForConsultation(APPLICATION_ID, CONSULTATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", PAGE_TITLE))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ConsulteeCaseProcessingController.class).getApplicationCaseProcessing(APPLICATION_ID, null))))
        .andExpect(model().attribute("applicationReference", APPLICATION_REFERENCE))
        .andExpect(model().attribute("consultationRequestView", ConsultationRequestView.from(consultation)))
        .andExpect(model().attribute("form", FurtherInformationRequestForm.empty()));
  }

  @Test
  void submitFurtherInformationToCaseOfficer() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consultationService.getOpenConsultationByIdAndApplication(CONSULTATION_ID, application)).thenReturn(consultation);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitFurtherInformationToCaseOfficer(APPLICATION_ID, CONSULTATION_ID, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("requestText", REQUEST_TEXT))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Further information requested for application %s".formatted(APPLICATION_REFERENCE))
            .build()));

    verify(furtherInformationService).saveFurtherInformationRequest(consultation, user, REQUEST_TEXT);
  }

  @ParameterizedTest
  @ValueSource(strings = {" ", ""})
  @NullSource
  void submitFurtherInformationToCaseOfficer_missingResponseText(String requestText) throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consultationService.getOpenConsultationByIdAndApplication(CONSULTATION_ID, application)).thenReturn(consultation);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);

    doAnswer(invocation -> {
      addSummarySectionsToModelAndView(invocation.getArgument(1, ModelAndView.class));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .submitFurtherInformationToCaseOfficer(APPLICATION_ID, CONSULTATION_ID, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("requestText", requestText))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(furtherInformationService, never()).saveFurtherInformationRequest(any(), any(), any());
  }

  private void addSummarySectionsToModelAndView(ModelAndView modelAndView) {
    modelAndView
        .addObject("summarySections", Collections.emptyList())
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", false);
  }

}
