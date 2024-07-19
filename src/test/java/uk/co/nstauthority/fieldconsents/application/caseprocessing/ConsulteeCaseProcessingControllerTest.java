package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_NUMBER;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.VIEW_APPLICATION;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.summary.ConsultationSummaryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ContextConfiguration(classes = ConsulteeCaseProcessingController.class)
class ConsulteeCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ConsulteeCaseProcessingController> CONTROLLER_CLASS = ConsulteeCaseProcessingController.class;
  private static final String PAGE_TITLE = "This is the page title";
  private static final String VIEW_NAME = "fcs/application/consultation/caseProcessing";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationContextService applicationContextService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private CaseProcessingTabService caseProcessingTabService;

  @MockBean
  private ConsultationSummaryService consultationSummaryService;

  private ApplicationVersion applicationVersion;

  private List<CaseProcessingActionView> caseProcessingActionViews;

  private List<SummarySection> summarySections;

  private List<CaseProcessingTab> caseProcessingTabs;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    caseProcessingActionViews = List.of(
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class)
    );
    summarySections = Collections.emptyList();
    caseProcessingTabs = CaseProcessingTab.CONSULTEE_TABS.stream().toList();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isConsulteeUser(user)).thenReturn(true);
  }

  @SecurityTest
  void caseProcessing_unauthorised() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS).caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void caseProcessing_noTabSelected_withoutConsultation() throws Exception {
    setUpMocksWithConsultation(null);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()));
  }

  @Test
  void caseProcessing_noTabSelected_withConsultation() throws Exception {
    var consultation = new Consultation();
    consultation.setRequestDeadline(Instant.now());

    setUpMocksWithConsultation(consultation);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("consultationRequestView", ConsultationRequestView.from(consultation)));
  }

  @Test
  void caseProcessing_viewApplication_withoutConsultation() throws Exception {
    setUpMocksWithConsultation(null);

    var tabParam = "?tab=%s".formatted(VIEW_APPLICATION.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)) + tabParam)
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));
  }

  @Test
  void caseProcessing_viewApplication_withConsultation() throws Exception {
    var consultation = new Consultation();
    consultation.setRequestDeadline(Instant.now());

    setUpMocksWithConsultation(consultation);

    var tabParam = "?tab=%s".formatted(VIEW_APPLICATION.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("consultationRequestView", ConsultationRequestView.from(consultation)));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));
  }

  @Test
  void caseProcessing_viewApplication_withVersionNumber() throws Exception {
    setUpMocksWithConsultationAndVersionNumber(null, APPLICATION_VERSION_NUMBER);

    var tabParam = "?tab=%s".formatted(VIEW_APPLICATION.getAnchor());
    var versionNumberParam = "&versionNumber=%d".formatted(APPLICATION_VERSION_NUMBER);

    when(applicationVersionService
        .getApplicationVersionByApplicationIdAndVersionNumber(APPLICATION_ID, APPLICATION_VERSION_NUMBER))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)) + tabParam + versionNumberParam)
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION))
        .andExpect(model().attribute("summarySections", summarySections))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));
  }

  @Test
  void caseProcessing_furtherInformation_withoutConsultation() throws Exception {
    setUpMocksWithConsultation(null);

    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication())).thenReturn(Collections.emptyList());

    var tabParam = "?tab=%s".formatted(CONSULTATIONS.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(CONSULTATIONS));
  }

  @Test
  void caseProcessing_furtherInformation_withConsultation() throws Exception {
    var consultation = new Consultation();
    consultation.setRequestDeadline(Instant.now());

    setUpMocksWithConsultation(consultation);

    var consultations = List.of(consultation);
    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication())).thenReturn(consultations);

    var tabParam = "?tab=%s".formatted(CONSULTATIONS.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpectAll(commonAttributesForTab(CONSULTATIONS))
        .andExpect(model().attribute("consultationRequestView", ConsultationRequestView.from(consultation)));
  }

  private ResultMatcher[] commonAttributesForTab(CaseProcessingTab tab) {
    var applicationType = applicationVersion.getApplication().getType();
    return new ResultMatcher[] {
        view().name(VIEW_NAME),
        model().attribute("controllerUrl", ReverseRouter.route(on(CONTROLLER_CLASS).caseProcessing(APPLICATION_ID, null,
            null, null))),
        model().attribute("pageTitle", PAGE_TITLE),
        model().attribute("selectedTab", tab),
        model().attribute("actionList", caseProcessingActionViews),
        model().attribute("caseProcessingTabs", caseProcessingTabs),
        model().attribute("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
    };
  }

  private void setUpMocksWithConsultation(
      @Nullable Consultation consultation
  ) {
    setUpMocksWithConsultationAndVersionNumber(consultation, null);
  }

  private void setUpMocksWithConsultationAndVersionNumber(
      @Nullable Consultation consultation,
      @Nullable Integer versionNumber
  ) {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationVersionService.getSelectedApplicationVersionOrCurrent(applicationVersion, versionNumber))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(PAGE_TITLE);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(caseProcessingActionViews);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(Optional.ofNullable(consultation));
    when(caseProcessingTabService.getConsulteeTabsAvailableToUser(user, applicationVersion)).thenReturn(caseProcessingTabs);

    var applicationType = applicationVersion.getApplication().getType();

    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject("summarySections", summarySections)
          .addObject("accordionId", applicationVersion.getId())
          .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(ModelAndView.class), eq(user));
  }
}
