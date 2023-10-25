package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CASE_HISTORY;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryTabContentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ContextConfiguration(classes = ApplicationCaseProcessingController.class)
class ApplicationCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationCaseProcessingController> CONTROLLER_CLASS = ApplicationCaseProcessingController.class;
  private static final String APPLICATION_REFERENCE = "application reference";
  private static final String VIEW_NAME = "fcs/application/applicationCaseProcessing";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private CaseProcessingTabService caseProcessingTabService;

  @MockBean
  private CaseHistoryTabContentService caseHistoryTabContentService;

  @MockBean
  private TechnicalReviewService technicalReviewService;

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private FurtherInformationService furtherInformationService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  private Consultation consultation;

  private FurtherInformation furtherInformation;

  private FurtherInformationView furtherInformationView;

  private List<CaseProcessingActionView> caseProcessingActionViews;

  private List<SummarySection> summarySections;

  private List<CaseProcessingTab> caseProcessingTabs;

  private List<CaseEventView> caseEventViews;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    technicalReview = new TechnicalReview();
    technicalReview.setDeadlineDateTime(Instant.now());
    technicalReview.setRequestText("request text");

    consultation = new Consultation();

    furtherInformation = new FurtherInformation();

    furtherInformationView = new FurtherInformationView(
        "timestamp",
        "user",
        "request text",
        true,
        "response timestamp",
        "response user",
        "response text"
    );

    caseProcessingActionViews = List.of(
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class)
    );

    summarySections = Collections.emptyList();
    caseProcessingTabs = EnumSet.allOf(CaseProcessingTab.class).stream().toList();

    caseEventViews = Collections.emptyList();
  }

  @SecurityTest
  void caseProcessing_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void caseProcessing_viewApplication() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall();

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpectAll(commonAttributesForTab(CaseProcessingTab.VIEW_APPLICATION, applicationVersion));
  }

  @Test
  void caseProcessing_viewApplication_isTechnicalReviewer_checkTechnicalReviewBannerExists() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall();

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.of(technicalReview));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .caseProcessing(APPLICATION_ID, null, null)))
        .with(user(user)))
        .andExpectAll(commonAttributesForTab(CaseProcessingTab.VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attribute("technicalReviewSummaryView", TechnicalReviewSummaryView.from(technicalReview)));
  }

  @Test
  void caseProcessing_viewApplication_isNotTechnicalReviewer_checkTechnicalReviewBannerDoesNotExist() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall();

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
        .caseProcessing(APPLICATION_ID, null, null)))
        .with(user(user)))
        .andExpectAll(commonAttributesForTab(CaseProcessingTab.VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attributeDoesNotExist("technicalReviewSummaryView"));
  }

  @Test
  void caseProcessing_viewApplication_isCaseOfficer_checkFurtherInformationBannerExists() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall();

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(Optional.of(consultation));
    when(furtherInformationService.findLatestOpenFurtherInformation(consultation)).thenReturn(Optional.of(furtherInformation));
    when(furtherInformationService.getFurtherInformationView(furtherInformation)).thenReturn(furtherInformationView);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CaseProcessingTab.VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attribute("furtherInformationView", furtherInformationView));
  }

  @Test
  void caseProcessing_viewApplication_isNotCaseOfficer_checkFurtherInformationBannerDoesNotExist() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall();

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CaseProcessingTab.VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attributeDoesNotExist("furtherInformationView"));
  }

  @Test
  void caseProcessing_caseHistory() throws Exception {
    stubBaseServiceCalls();
    stubCaseHistoryServiceCall();

    var tabParam = "?tab=%s".formatted(CASE_HISTORY.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CASE_HISTORY, applicationVersion))
        .andExpect(model().attribute("caseHistoryEvents", caseEventViews));
  }

  @Test
  void caseProcessing_caseHistory_isTechnicalReviewer_checkTechnicalReviewBannerExists() throws Exception {
    stubBaseServiceCalls();
    stubCaseHistoryServiceCall();

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.of(technicalReview));

    var tabParam = "?tab=%s".formatted(CASE_HISTORY.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CASE_HISTORY, applicationVersion))
        .andExpect(model().attribute("caseHistoryEvents", caseEventViews))
        .andExpect(model().attribute("technicalReviewSummaryView", TechnicalReviewSummaryView.from(technicalReview)));
  }

  @Test
  void caseProcessing_caseHistory_isNotTechnicalReviewer_checkTechnicalReviewBannerDoesNotExist() throws Exception {
    stubBaseServiceCalls();
    stubCaseHistoryServiceCall();

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);

    var tabParam = "?tab=%s".formatted(CASE_HISTORY.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CASE_HISTORY, applicationVersion))
        .andExpect(model().attribute("caseHistoryEvents", caseEventViews))
        .andExpect(model().attributeDoesNotExist("technicalReviewSummaryView"));
  }

  @Test
  void caseProcessing_caseHistory_isCaseOfficer_checkFurtherInformationBannerExists() throws Exception {
    stubBaseServiceCalls();
    stubCaseHistoryServiceCall();

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(Optional.of(consultation));
    when(furtherInformationService.findLatestOpenFurtherInformation(consultation)).thenReturn(Optional.of(furtherInformation));
    when(furtherInformationService.getFurtherInformationView(furtherInformation)).thenReturn(furtherInformationView);

    var tabParam = "?tab=%s".formatted(CASE_HISTORY.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CASE_HISTORY, applicationVersion))
        .andExpect(model().attribute("caseHistoryEvents", caseEventViews))
        .andExpect(model().attribute("furtherInformationView", furtherInformationView));
  }

  @Test
  void caseProcessing_caseHistory_isNotCaseOfficer_checkFurtherInformationBannerDoesNotExist() throws Exception {
    stubBaseServiceCalls();
    stubCaseHistoryServiceCall();

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);

    var tabParam = "?tab=%s".formatted(CASE_HISTORY.getAnchor());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null)) + tabParam)
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CASE_HISTORY, applicationVersion))
        .andExpect(model().attribute("caseHistoryEvents", caseEventViews))
        .andExpect(model().attributeDoesNotExist("furtherInformationView"));
  }

  private void stubBaseServiceCalls() {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(caseProcessingTabService.getTabsAvailableToUser(user)).thenReturn(caseProcessingTabs);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(caseProcessingActionViews);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
  }

  private void stubSummaryServiceCall() {
    var applicationType = applicationVersion.getApplication().getType();
    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject("summarySections", summarySections)
          .addObject("accordionId", applicationVersion.getId())
          .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType));
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsToModelAndView(eq(applicationVersion), any(ModelAndView.class));
  }

  private void stubCaseHistoryServiceCall() {
    var application = applicationVersion.getApplication();
    when(caseHistoryTabContentService.getCaseHistoryTabContent(application)).thenReturn(caseEventViews);
  }

  private ResultMatcher[] commonAttributesForTab(CaseProcessingTab tab, ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    return new ResultMatcher[]{
        status().isOk(),
        view().name(VIEW_NAME),
        model().attribute("controllerUrl", ReverseRouter.route(on(CONTROLLER_CLASS).caseProcessing(APPLICATION_ID, null, null))),
        model().attribute("pageTitle", APPLICATION_REFERENCE),
        model().attribute("selectedTab", tab),
        model().attribute("actionList", caseProcessingActionViews),
        model().attribute("caseProcessingTabs", caseProcessingTabs),
        model().attribute("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
    };
  }
}
