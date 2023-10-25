package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.response;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = FurtherInformationResponseController.class)
class FurtherInformationResponseControllerTest extends AbstractApplicationControllerTest {

  private static final Class<FurtherInformationResponseController> CONTROLLER_CLASS = FurtherInformationResponseController.class;
  private static final String PAGE_TITLE = "Further information response";
  private static final String VIEW_NAME = "fcs/application/consultation/further-information/responseForm";
  private static final String APPLICATION_REFERENCE = "application reference";
  private static final String RESPONSE_TEXT = "response text";

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

  private FurtherInformation furtherInformation;

  private FurtherInformationView furtherInformationView;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    application = applicationVersion.getApplication();

    consultation = new Consultation();
    furtherInformation = new FurtherInformation();

    furtherInformationView = new FurtherInformationView(
        "timestamp",
        "user",
        "request text",
        false,
        null,
        null,
        null
    );

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getResponseForm_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).getResponseForm(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getResponseForm() throws Exception {
    mockServiceCalls();

    doAnswer(invocation -> {
      var modelAndView = invocation.getArgument(1, ModelAndView.class);
      addSummarySections(modelAndView);
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getResponseForm(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("pageTitle", PAGE_TITLE))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(model().attribute("applicationReference", APPLICATION_REFERENCE))
        .andExpect(model().attribute("furtherInformationView", furtherInformationView))
        .andExpect(model().attribute("form", FurtherInformationResponseForm.empty()));
  }

  @Test
  void submitResponseForm() throws Exception {
    mockServiceCalls();

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getResponseForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf())
            .param("responseText", RESPONSE_TEXT))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .andExpect(notificationBanner(NotificationBanner.builder()
            .withBannerType(NotificationBannerType.SUCCESS)
            .withHeadingContent("Further information response submitted for application %s".formatted(APPLICATION_REFERENCE))
            .build()));

    verify(furtherInformationService).saveFurtherInformationResponse(furtherInformation, user, RESPONSE_TEXT);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " "})
  @NullSource
  void submitResponseForm_noResponseText(String responseText) throws Exception {
    mockServiceCalls();

    doAnswer(invocation -> {
      var modelAndView = invocation.getArgument(1, ModelAndView.class);
      addSummarySections(modelAndView);
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getResponseForm(APPLICATION_ID)))
            .with(user(user))
            .with(csrf())
            .param("responseText", responseText))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name(VIEW_NAME));

    verify(furtherInformationService, never()).saveFurtherInformationResponse(any(), any(), any());
  }

  private void mockServiceCalls() {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consultationService.getLatestOpenConsultation(application)).thenReturn(consultation);
    when(furtherInformationService.getLatestOpenFurtherInformation(consultation)).thenReturn(furtherInformation);
    when(furtherInformationService.getFurtherInformationView(furtherInformation)).thenReturn(furtherInformationView);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
  }

  private void addSummarySections(ModelAndView modelAndView) {
    modelAndView
        .addObject("summarySections", Collections.emptyList())
        .addObject("accordionId", "123")
        .addObject("wideSummaryDisplay", true);
  }

}
