package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ConsulteeCaseProcessingController.class)
class ConsulteeCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ConsulteeCaseProcessingController> CONTROLLER_CLASS = ConsulteeCaseProcessingController.class;
  private static final String PAGE_TITLE = "This is the page title";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ConsultationService consultationService;

  private ApplicationVersion applicationVersion;

  private ModelAndView emptyModelAndView;

  private List<CaseProcessingActionView> caseProcessingActionViews;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    emptyModelAndView = new ModelAndView();
    caseProcessingActionViews = List.of(
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class)
    );

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getApplicationCaseProcessing_unauthorised() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getApplicationCaseProcessing_withoutConsultation() throws Exception {
    setUpMocksWithConsultation(null);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .getApplicationCaseProcessing(APPLICATION_ID, null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model).containsEntry("actionList", caseProcessingActionViews);
  }

  @Test
  void getApplicationCaseProcessing_withConsultation() throws Exception {
    var consultation = new Consultation();
    consultation.setRequestDeadline(Instant.now());

    setUpMocksWithConsultation(consultation);

    var model = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model)
        .containsEntry("actionList", caseProcessingActionViews)
        .containsEntry("consultationRequestView", ConsultationRequestView.from(consultation));
  }

  private void setUpMocksWithConsultation(@Nullable Consultation consultation) {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(PAGE_TITLE);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(caseProcessingActionViews);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(Optional.ofNullable(consultation));
    when(applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/consultation/caseProcessing",
        PAGE_TITLE,
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
    )).thenReturn(emptyModelAndView);
  }

}
