package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CASE_HISTORY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CONSENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.PAYMENTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.TASKS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.VIEW_APPLICATION;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryTabContentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabConsentSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ProductionConsentCheckResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches.ConsentBreach;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches.ConsentBreachService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnersView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabPaymentSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawal;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FormattedFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.licences.LicenceView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ContextConfiguration(classes = ApplicationCaseProcessingController.class)
class ApplicationCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final Class<ApplicationCaseProcessingController> CONTROLLER_CLASS = ApplicationCaseProcessingController.class;
  private static final String APPLICATION_REFERENCE = "application reference";
  private static final String VIEW_NAME = "fcs/application/applicationCaseProcessing";

  private static final String TECHNICAL_REVIEW_ATTRIBUTE = "technicalReviewSummaryView";
  private static final String FURTHER_INFORMATION_ATTRIBUTE = "furtherInformationView";
  private static final String TASK_LIST_ATTRIBUTE = "taskListSections";
  private static final String SUMMARY_SECTIONS_ATTRIBUTE = "summarySections";
  private static final String CASE_HISTORY_ATTRIBUTE = "caseHistoryEvents";
  private static final String PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE = "paymentsTabPaymentSummaryViews";
  private static final String CONSENT_TAB_CONSENT_SUMMARY_VIEW_ATTRIBUTE = "consentTabConsentSummaryView";
  private static final String CONSENT_ISSUING_APPROVAL_SUMMARY_VIEW_ATTRIBUTE = "consentIssuingApprovalSummaryView";
  private static final String IS_MIGRATED_APPLICATION_ATTRIBUTE = "isMigratedApplication";
  private static final String OPEN_WITHDRAWAL_ATTRIBUTE = "openWithdrawal";
  private static final String CONSENT_EXCEEDED_ATTRIBUTE = "isConsentBreached";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationContextService applicationContextService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationWithdrawalService applicationWithdrawalService;

  @MockBean
  private CaseProcessingTabService caseProcessingTabService;

  @MockBean
  private CaseProcessingTaskListService caseProcessingTaskListService;

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

  @MockBean
  private PaymentsTabService paymentsTabService;

  @MockBean
  private ConsentTabService consentTabService;

  @MockBean
  private ConsentIssuingApprovalService consentIssuingApprovalService;

  @MockBean
  private ConsentService consentService;

  @MockBean
  private ConsentBreachService consentBreachService;

  @MockBean
  private CaseProcessingControllerHelperService caseProcessingControllerHelperService;

  @MockBean
  private LicenceExpiryService licenceExpiryService;

  private ApplicationVersion applicationVersion;

  private Application application;

  private TechnicalReview technicalReview;

  private Consultation consultation;

  private FurtherInformation furtherInformation;

  private FurtherInformationView furtherInformationView;

  private List<CaseProcessingActionView> caseProcessingActionViews;

  private List<SummarySection> summarySections;

  private List<CaseProcessingTab> caseProcessingTabs;

  private List<CaseEventView> caseEventViews;

  private List<TaskListSection> taskListSections;

  private List<PaymentsTabPaymentSummaryView> paymentsTabPaymentSummaryViews;

  private ConsentTabConsentSummaryView consentTabConsentSummaryView;

  private ConsentIssuingApprovalSummaryView consentIssuingApprovalSummaryView;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

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

    consentIssuingApprovalSummaryView = new ConsentIssuingApprovalSummaryView(
        "Test user (test@SecurityTest.com)",
        "6 Mar 2024 11:18"
    );

    summarySections = Collections.emptyList();
    caseProcessingTabs = CaseProcessingTab.REGULATOR_TABS.stream().toList();
    caseEventViews = Collections.emptyList();
    taskListSections = Collections.emptyList();

    paymentsTabPaymentSummaryViews = List.of(new PaymentsTabPaymentSummaryView(
        "testStatus",
        "testDescription",
        "testFormattedPaymentAmount",
        "testPaidByUser",
        "testFormattedPaymentDate",
        "testGovUkPayReference"
    ));

    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(
        ProductionUnit.KSCM_PER_DAY);
    var consentFieldEquityPartnersView = new ConsentFieldEquityPartnersView(
        List.of(new FormattedFieldEquityPartner("ORG1", "12345678")
            , new FormattedFieldEquityPartner("ORG2", "87654321")
            , new FormattedFieldEquityPartner("ORG3", null)
        )
    );

    consentTabConsentSummaryView = new ConsentTabConsentSummaryView(
        ApplicationType.PRODUCTION,
        ConsentLengthType.ANNUAL,
        "Test issued by user",
        "04/04/2024",
        ConsentStatus.ISSUED,
        null,
        consentDataView,
        consentFigureUnitView,
        consentFieldEquityPartnersView,
        List.of(new SummaryFileView("Test file name", "Test description", "http://test.url"))
    );

    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isRegulatorUser(user)).thenReturn(true);
  }

  @ParameterizedSecurityTest
  @EnumSource(CaseProcessingTab.class)
  @NullSource
  void caseProcessing_noUser(CaseProcessingTab caseProcessingTab) throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, caseProcessingTab, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void caseProcessing_checkProductionConsentWarning() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    var checkResult = ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(true);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion)).thenReturn(
        checkResult);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(model().attribute("warning", checkResult.getWarning()));
  }

  @ParameterizedTest
  @EnumSource(
      value = ProductionConsentCheckResult.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = "NOT_WITHIN_ACTIVE_CONSENT"
  )
  void caseProcessing_checkProductionConsentWarning_ignoredResults(ProductionConsentCheckResult checkResult) throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(true);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion)).thenReturn(
        checkResult);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(model().attributeDoesNotExist("warning"));
  }

  @Test
  void caseProcessing_checkProductionConsentWarning_facilityNotField() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    var checkResult = ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT;
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion)).thenReturn(
        checkResult);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(model().attributeDoesNotExist("warning"));
  }

  @Test
  void caseProcessing_checkProductionConsentWarning_activeConsentExists() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(true);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion))
        .thenReturn(ProductionConsentCheckResult.WITHIN_ACTIVE_CONSENT);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(model().attributeDoesNotExist("warning"));
  }

  @Test
  void getIndustryCaseProcessing_withLicenceExpiringWithinDuration() throws Exception {
    var licenceView = new LicenceView("Test123", "25th of December 2024");
    var expiringLicences = List.of(licenceView);

    stubBaseServiceCalls();
    stubConsentServiceCall();
    stubSummaryServiceCall(applicationVersion);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion))
        .thenReturn(expiringLicences);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("expiringLicences", expiringLicences));
  }

  @Test
  void getIndustryCaseProcessing_withoutLicenceExpiringWithinDuration() throws Exception {
    List<LicenceView> expiringLicences = List.of();

    stubBaseServiceCalls();
    stubConsentServiceCall();
    stubSummaryServiceCall(applicationVersion);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion))
        .thenReturn(expiringLicences);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("expiringLicences", List.of()));
  }


  @Test
  void caseProcessing_isTechnicalReviewer_checkTechnicalReviewBannerExists() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall(applicationVersion);

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.of(technicalReview));
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, TASKS, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(TASKS, applicationVersion))
        .andExpect(model().attribute(TECHNICAL_REVIEW_ATTRIBUTE, TechnicalReviewSummaryView.from(technicalReview)));
  }

  @Test
  void caseProcessing_isNotTechnicalReviewer_checkTechnicalReviewBannerDoesNotExist() throws Exception {
    stubBaseServiceCalls();

    when(regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, TASKS, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(TASKS, applicationVersion))
        .andExpect(model().attributeDoesNotExist(TECHNICAL_REVIEW_ATTRIBUTE));
  }

  @Test
  void caseProcessing_isCaseOfficer_checkFurtherInformationBannerExists() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall(applicationVersion);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(
        Optional.of(consultation));
    when(furtherInformationService.findLatestOpenFurtherInformation(consultation)).thenReturn(
        Optional.of(furtherInformation));
    when(furtherInformationService.getFurtherInformationView(furtherInformation)).thenReturn(furtherInformationView);
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attribute(FURTHER_INFORMATION_ATTRIBUTE, furtherInformationView));
  }

  @Test
  void caseProcessing_isNotCaseOfficer_checkFurtherInformationBannerDoesNotExist() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall(applicationVersion);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(false);
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attributeDoesNotExist(FURTHER_INFORMATION_ATTRIBUTE));
  }

  @Test
  void caseProcessing_whenCaseIsReadyToGrantAndIssue_thenConsentIssuingApprovalSummaryViewExists() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall(applicationVersion);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(
        Optional.empty());
    when(furtherInformationService.findLatestOpenFurtherInformation(consultation)).thenReturn(Optional.empty());

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(true);
    when(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application))
        .thenReturn(Optional.of(consentIssuingApprovalSummaryView));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(
            model().attribute(CONSENT_ISSUING_APPROVAL_SUMMARY_VIEW_ATTRIBUTE, consentIssuingApprovalSummaryView));
  }

  @Test
  void caseProcessing_whenCaseIsConsented_thenConsentIssuingApprovalSummaryViewDoesNotExist() throws Exception {
    applicationVersion = ApplicationTestUtil.getConsentedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    stubBaseServiceCalls();
    stubSummaryServiceCall(applicationVersion);

    when(regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user.wuaId()))).thenReturn(true);
    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication())).thenReturn(
        Optional.empty());
    when(furtherInformationService.findLatestOpenFurtherInformation(consultation)).thenReturn(Optional.empty());

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(true);
    when(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)).thenReturn(
        Optional.of(consentIssuingApprovalSummaryView));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andReturn().getModelAndView();

    assert modelAndView != null;

    assertThat(modelAndView.getModel())
        .doesNotContainKey(CONSENT_ISSUING_APPROVAL_SUMMARY_VIEW_ATTRIBUTE);
  }

  @Test
  void caseProcessing_noTabSelected() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(TASKS, applicationVersion))
        .andExpect(model().attribute(TASK_LIST_ATTRIBUTE, taskListSections))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  @Test
  void caseProcessing_noTabSelected_withdrawnBanner() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    var applicationWithdrawal = new ApplicationWithdrawal();
    when(applicationWithdrawalService.findOpenApplicationWithdrawal(applicationVersion))
        .thenReturn(Optional.of(applicationWithdrawal));


    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(OPEN_WITHDRAWAL_ATTRIBUTE, true))
        .andExpect(model().attribute(TASK_LIST_ATTRIBUTE, taskListSections))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  @Test
  void caseProcessing_noTabSelected_noWithdrawnBanner() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    when(applicationWithdrawalService.findOpenApplicationWithdrawal(applicationVersion))
        .thenReturn(Optional.empty());


    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(OPEN_WITHDRAWAL_ATTRIBUTE, false))
        .andExpect(model().attribute(TASK_LIST_ATTRIBUTE, taskListSections))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  @Test
  void caseProcessing_exceededBanner() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    var consentBreach = new ConsentBreach();
    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.of(consentBreach));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(CONSENT_EXCEEDED_ATTRIBUTE, true));
  }

  @Test
  void caseProcessing_noExceededBanner() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    when(consentBreachService.findConsentBreachByApplication(application))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(CONSENT_EXCEEDED_ATTRIBUTE, false));
  }

  @Test
  void caseProcessing_tasks() throws Exception {
    stubBaseServiceCalls();
    stubTaskListServiceCall();

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, TASKS, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(TASKS, applicationVersion))
        .andExpect(model().attribute(TASK_LIST_ATTRIBUTE, taskListSections))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  @Test
  void caseProcessing_viewApplication_withVersion() throws Exception {
    var requestedApplicationVersionId = 123;
    var requestedApplicationVersion = new ApplicationVersion();
    requestedApplicationVersion.setId(requestedApplicationVersionId);
    requestedApplicationVersion.setApplication(application);

    stubBaseServiceCalls();
    stubSummaryServiceCall(requestedApplicationVersion);

    when(caseProcessingControllerHelperService.getApplicationVersionForApplication(application,
        requestedApplicationVersionId))
        .thenReturn(requestedApplicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, requestedApplicationVersionId, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attributeDoesNotExist(TASK_LIST_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attribute(SUMMARY_SECTIONS_ATTRIBUTE, summarySections));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(requestedApplicationVersion),
        any(), eq(user));
  }

  @Test
  void caseProcessing_viewApplication_withoutVersion() throws Exception {
    stubBaseServiceCalls();
    stubSummaryServiceCall(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attributeDoesNotExist(TASK_LIST_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attribute(SUMMARY_SECTIONS_ATTRIBUTE, summarySections));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(),
        eq(user));
  }

  @Test
  void caseProcessing_caseHistory() throws Exception {
    stubBaseServiceCalls();
    stubCaseHistoryServiceCall();

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, CASE_HISTORY, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CASE_HISTORY, applicationVersion))
        .andExpect(model().attributeDoesNotExist(TASK_LIST_ATTRIBUTE))
        .andExpect(model().attribute(CASE_HISTORY_ATTRIBUTE, caseEventViews))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  @Test
  void caseProcessing_payments() throws Exception {
    stubBaseServiceCalls();
    stubPaymentsServiceCall();

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, PAYMENTS, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(PAYMENTS, applicationVersion))
        .andExpect(model().attributeDoesNotExist(TASK_LIST_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attribute(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE, paymentsTabPaymentSummaryViews))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  @Test
  void caseProcessing_consent() throws Exception {
    stubBaseServiceCalls();
    stubConsentServiceCall();

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER_CLASS)
            .caseProcessing(APPLICATION_ID, null, CONSENT, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CONSENT, applicationVersion))
        .andExpect(model().attributeDoesNotExist(TASK_LIST_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(CASE_HISTORY_ATTRIBUTE))
        .andExpect(model().attributeDoesNotExist(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE))
        .andExpect(model().attribute(CONSENT_TAB_CONSENT_SUMMARY_VIEW_ATTRIBUTE, consentTabConsentSummaryView))
        .andExpect(model().attributeDoesNotExist(SUMMARY_SECTIONS_ATTRIBUTE));
  }

  private void stubBaseServiceCalls() {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(
        Optional.of(applicationVersion));

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(
        applicationVersion);
    when(caseProcessingTabService.getRegulatorTabsAvailableToUser(user, applicationVersion)).thenReturn(
        caseProcessingTabs);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(
        caseProcessingActionViews);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());
    when(applicationService
        .getApplicationReference(applicationVersion,
            applicationVersion.getApplication().getType().getDisplayName() + " application"))
        .thenReturn(APPLICATION_REFERENCE);
    when(applicationService.isMigratedApplication(application)).thenReturn(false);
  }

  private void stubTaskListServiceCall() {
    when(caseProcessingTaskListService.getTaskListSections(applicationVersion, user)).thenReturn(taskListSections);
  }

  private void stubSummaryServiceCall(ApplicationVersion applicationVersion) {
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

  private void stubCaseHistoryServiceCall() {
    when(caseHistoryTabContentService.getCaseHistoryTabContent(application)).thenReturn(caseEventViews);
  }

  private void stubPaymentsServiceCall() {
    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject(PAYMENTS_TAB_PAYMENT_SUMMARY_VIEWS_ATTRIBUTE, paymentsTabPaymentSummaryViews);
      return null;
    })
        .when(paymentsTabService)
        .addPaymentsTabContentToModelAndView(eq(application), any(ModelAndView.class));
  }

  private void stubConsentServiceCall() {
    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject(CONSENT_TAB_CONSENT_SUMMARY_VIEW_ATTRIBUTE, consentTabConsentSummaryView);
      return null;
    })
        .when(consentTabService)
        .addConsentTabContentToModelAndView(eq(applicationVersion), any(ModelAndView.class));
  }

  private ResultMatcher[] commonAttributesForTab(CaseProcessingTab tab, ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    return new ResultMatcher[]{
        status().isOk(),
        view().name(VIEW_NAME),
        model().attribute("controllerUrl", ReverseRouter.route(on(CONTROLLER_CLASS).caseProcessing(APPLICATION_ID, null,
            null, null
        ))),
        model().attribute("pageTitle", APPLICATION_REFERENCE),
        model().attribute("selectedTab", tab),
        model().attribute("actionList", caseProcessingActionViews),
        model().attribute("caseProcessingTabs", caseProcessingTabs),
        model().attribute("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType)),
        model().attribute(IS_MIGRATED_APPLICATION_ATTRIBUTE, false)
    };
  }

}
