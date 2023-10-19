package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryTabContentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequest;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ContextConfiguration(classes = ApplicationCaseProcessingController.class)
class ApplicationCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final String VIEW_NAME = "fcs/application/applicationCaseProcessing";
  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

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
  private FurtherInformationRequestService furtherInformationRequestService;

  private TechnicalReview technicalReview;

  private Consultation consultation;

  private FurtherInformationRequest furtherInformationRequest;

  private FurtherInformationRequestView furtherInformationRequestView;

  @BeforeEach
  void setUp() {
    technicalReview = new TechnicalReview();
    technicalReview.setDeadlineDateTime(Instant.now());
    technicalReview.setRequestText("request text");

    consultation = new Consultation();

    furtherInformationRequest = new FurtherInformationRequest();

    furtherInformationRequestView = new FurtherInformationRequestView("timestamp", "user", "request text");
  }

  @SecurityTest
  void getApplicationCaseProcessing_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewApplicationTab_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getViewApplicationTab(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getCaseHistoryTab_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getCaseHistoryTab(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    when(caseProcessingTabService.getTabsAvailableToUser(user)).thenReturn(Arrays.asList(CaseProcessingTab.values()));
    var caseHistoryEvents = CaseHistoryEventTestUtil.getMockCaseEventViews();
    when(caseHistoryTabContentService.getCaseHistoryTabContent(applicationVersion.getApplication()))
        .thenReturn(caseHistoryEvents);
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .thenReturn(Optional.of(technicalReview));

    var actionViews =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP, applicationVersion));
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .thenReturn(actionViews);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any());
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationCaseProcessing"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    assertModel(applicationVersion, caseHistoryEvents, actionViews, modelAndView, CaseProcessingTab.VIEW_APPLICATION);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getViewApplicationTab(ApplicationVersion applicationVersion) throws Exception {
    when(caseProcessingTabService.getTabsAvailableToUser(user)).thenReturn(Arrays.asList(CaseProcessingTab.values()));
    var caseHistoryEvents = CaseHistoryEventTestUtil.getMockCaseEventViews();
    when(caseHistoryTabContentService.getCaseHistoryTabContent(applicationVersion.getApplication()))
        .thenReturn(caseHistoryEvents);

    var actionViews =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP, applicationVersion));
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .thenReturn(actionViews);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any());
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getViewApplicationTab(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationCaseProcessing"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    assertModel(applicationVersion, caseHistoryEvents, actionViews, modelAndView, CaseProcessingTab.VIEW_APPLICATION);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getCaseHistoryTab(ApplicationVersion applicationVersion) throws Exception {
    when(caseProcessingTabService.getTabsAvailableToUser(user)).thenReturn(Arrays.asList(CaseProcessingTab.values()));
    var caseHistoryEvents = CaseHistoryEventTestUtil.getMockCaseEventViews();
    when(caseHistoryTabContentService.getCaseHistoryTabContent(applicationVersion.getApplication()))
        .thenReturn(caseHistoryEvents);

    var actionViews =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP, applicationVersion));
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .thenReturn(actionViews);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any());
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getCaseHistoryTab(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationCaseProcessing"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    assertModel(applicationVersion, caseHistoryEvents, actionViews, modelAndView, CaseProcessingTab.CASE_HISTORY);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationCaseProcessing_isTechnicalReviewer_checkTechnicalReviewBannerExists(ApplicationVersion applicationVersion) throws Exception {
    stubModelAndViewCalls(applicationVersion);

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.of(technicalReview));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
        .getApplicationCaseProcessing(APPLICATION_ID, null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("technicalReviewSummaryView", TechnicalReviewSummaryView.from(technicalReview)));
  }

  private void stubModelAndViewCalls(ApplicationVersion applicationVersion) {
    when(caseProcessingTabService.getTabsAvailableToUser(user)).thenReturn(Arrays.asList(CaseProcessingTab.values()));
    var caseHistoryEvents = CaseHistoryEventTestUtil.getMockCaseEventViews();
    when(caseHistoryTabContentService.getCaseHistoryTabContent(applicationVersion.getApplication())).thenReturn(caseHistoryEvents);

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    var actionViews = List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP, applicationVersion));
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(actionViews);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(applicationSummaryService.getApplicationSummaryModelAndView(applicationVersion, VIEW_NAME, DUMMY_APP_REF)).thenReturn(new ModelAndView());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationCaseProcessing_isNotTechnicalReviewer_checkTechnicalReviewBannerDoesNotExist(ApplicationVersion applicationVersion) throws Exception {
    stubModelAndViewCalls(applicationVersion);

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);

    var model = mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model).doesNotContainKey("technicalReviewSummaryView");
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationCaseProcessing_isCaseOfficer_checkFurtherInformationBannerExists(ApplicationVersion applicationVersion) throws Exception {
    stubModelAndViewCalls(applicationVersion);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(Optional.of(consultation));
    when(furtherInformationRequestService.findLatestOpenFurtherInformationRequest(consultation)).thenReturn(Optional.of(furtherInformationRequest));
    when(furtherInformationRequestService.getFurtherInformationRequestView(furtherInformationRequest)).thenReturn(furtherInformationRequestView);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("furtherInformationRequestView", furtherInformationRequestView));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationCaseProcessing_isNotCaseOfficer_checkFurtherInformationBannerDoesNotExist(ApplicationVersion applicationVersion) throws Exception {
    stubModelAndViewCalls(applicationVersion);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);

    var model = mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(model).doesNotContainKey("furtherInformationRequestView");
  }

  private static void assertModel(ApplicationVersion applicationVersion,
                                  List<CaseEventView> caseHistoryEvents,
                                  List<CaseProcessingActionView> actionViews,
                                  ModelAndView modelAndView,
                                  CaseProcessingTab caseHistory) {
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", DUMMY_APP_REF)
        .containsKey("summarySections")
        .containsEntry("accordionId", applicationVersion.getId())
        .containsKey("wideSummaryDisplay")
        .containsEntry("actionList", actionViews)
        .containsEntry("applicationId", applicationVersion.getApplication().getId())
        .containsEntry("selectedTab", caseHistory.getValue())
        .containsEntry("caseHistoryEvents", caseHistoryEvents);
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
