package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CONSENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.PAYMENTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.VIEW_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.applicationUpdateRequestView;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabPaymentSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawal;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationVersionView;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListService;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.licences.LicenceView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = IndustryCaseProcessingController.class)
class IndustryCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";
  private static final String VIEW_NAME = "fcs/application/industryCaseProcessing";
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
  private ApplicationUpdateService applicationUpdateService;

  @MockBean
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @MockBean
  private PaymentsTabService paymentsTabService;

  @MockBean
  private ConsentTabService consentTabService;

  @MockBean
  private ConsentService consentService;

  @MockBean
  private ConsentBreachService consentBreachService;

  @MockBean
  private CaseProcessingControllerHelperService caseProcessingControllerHelperService;

  @MockBean
  private ApplicationTaskListService applicationTaskListService;

  @MockBean
  private LicenceExpiryService licenceExpiryService;

  private List<CaseProcessingActionView> caseProcessingActionViews;

  private List<SummarySection> summarySections;

  private List<CaseProcessingTab> caseProcessingTabs;

  private List<PaymentsTabPaymentSummaryView> paymentsTabPaymentSummaryViews;

  private ConsentTabConsentSummaryView consentTabConsentSummaryView;

  @BeforeEach
  void setUp() {
    caseProcessingActionViews = List.of(
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class),
        mock(CaseProcessingActionView.class)
    );

    summarySections = Collections.emptyList();
    caseProcessingTabs = CaseProcessingTab.INDUSTRY_TABS.stream().toList();

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

    consentTabConsentSummaryView = new ConsentTabConsentSummaryView(
        ApplicationType.PRODUCTION,
        ConsentLengthType.ANNUAL,
        "Test issued by user",
        "04/04/2024",
        ConsentStatus.ISSUED,
        null,
        consentDataView,
        consentFigureUnitView,
        null,
        List.of(new SummaryFileView("Test file name", "Test description", "http://test.url"))
    );

    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);
  }

  @SecurityTest
  void getIndustryCaseProcessing_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedSecurityTest
  @EnumSource(CaseProcessingTab.class)
  @NullSource
  void getIndustryCaseProcessing_forTabs_noUser(CaseProcessingTab caseProcessingTab) throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, caseProcessingTab, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getIndustryCaseProcessing_checkEndPointSecurityOnly_whenConsentedStatus_thenForbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(ApplicationVersionStatus.CONSENTED);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(Set.of());
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getIndustryCaseProcessing_checkEndPointSecurityOnly_whenDeletedStatus_thenForbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(ApplicationVersionStatus.DELETED);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getIndustryCaseProcessing_checkEndPointSecurityOnly_whenWithdrawnStatus_thenForbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getIndustryCaseProcessing_checkEndPointSecurityOnly_whenMissingEditPermissionAndPayAndSubmitPermissionAndViewFcsConsentsPermission_thenForbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.EDIT_FCS_APPLICATIONS,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS,
        RolePermission.VIEW_FCS_CONSENTS
    )).thenReturn(false);

    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_noTabSelected(ApplicationVersion applicationVersion) throws Exception {
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verifyNoInteractions(applicationUpdateRequestViewService);
  }

  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_viewApplication(ApplicationVersion applicationVersion) throws Exception {
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verifyNoInteractions(applicationUpdateRequestViewService);
  }

  @Test
  void getIndustryCaseProcessing_viewApplication_withVersion() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var requestedApplicationVersionId = 123;
    var requestedApplicationVersion = new ApplicationVersion();
    requestedApplicationVersion.setId(requestedApplicationVersionId);
    requestedApplicationVersion.setApplication(application);

    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(requestedApplicationVersion);

    when(caseProcessingControllerHelperService.getApplicationVersionForApplication(application, requestedApplicationVersionId))
        .thenReturn(requestedApplicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, requestedApplicationVersionId, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attributeDoesNotExist("paymentsTabPaymentSummaryViews"))
        .andExpect(model().attribute("summarySections", summarySections));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(requestedApplicationVersion), any(), eq(user));
  }

  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_viewApplication_withApplicationUpdateStarted(ApplicationVersion applicationVersion) throws Exception {
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(true);
    when(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .thenReturn(applicationUpdateRequestView);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(VIEW_APPLICATION, applicationVersion))
        .andExpect(model().attribute("applicationUpdateRequestView", applicationUpdateRequestView));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verify(applicationUpdateRequestViewService).getOpenApplicationUpdateRequestView(applicationVersion);
  }

  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_payments(ApplicationVersion applicationVersion) throws Exception {
    var application = applicationVersion.getApplication();

    stubBaseServiceCalls(applicationVersion);
    stubPaymentsServiceCall(application);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, PAYMENTS, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(PAYMENTS, applicationVersion));

    verify(paymentsTabService).addPaymentsTabContentToModelAndView(eq(application), any());

    verifyNoInteractions(applicationUpdateRequestViewService);
  }

  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_payments_withApplicationUpdateStarted(ApplicationVersion applicationVersion) throws Exception {
    var application = applicationVersion.getApplication();

    stubBaseServiceCalls(applicationVersion);
    stubPaymentsServiceCall(application);

    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(true);
    when(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .thenReturn(applicationUpdateRequestView);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, PAYMENTS, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(PAYMENTS, applicationVersion))
        .andExpect(model().attribute("applicationUpdateRequestView", applicationUpdateRequestView));

    verify(paymentsTabService).addPaymentsTabContentToModelAndView(eq(application), any());

    verify(applicationUpdateRequestViewService).getOpenApplicationUpdateRequestView(applicationVersion);
  }

  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_consent(ApplicationVersion applicationVersion) throws Exception {
    stubBaseServiceCalls(applicationVersion);
    stubConsentServiceCall(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, CONSENT, null)))
            .with(user(user)))
        .andExpectAll(commonAttributesForTab(CONSENT, applicationVersion));

    verify(consentTabService).addConsentTabContentToModelAndView(eq(applicationVersion), any());

    verifyNoInteractions(applicationUpdateRequestViewService);
  }


  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_noTabSelected_withdrawnBanner(ApplicationVersion applicationVersion) throws Exception {
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    var applicationWithdrawal = new ApplicationWithdrawal();
    when(applicationWithdrawalService.findOpenApplicationWithdrawal(applicationVersion))
        .thenReturn(Optional.of(applicationWithdrawal));

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(OPEN_WITHDRAWAL_ATTRIBUTE, true));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verifyNoInteractions(applicationUpdateRequestViewService);
  }


  @ParameterizedTest
  @MethodSource("getInProgressAndSubmittedApplicationVersions")
  void getIndustryCaseProcessing_noTabSelected_noWithdrawnBanner(ApplicationVersion applicationVersion) throws Exception {
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(applicationWithdrawalService.findOpenApplicationWithdrawal(applicationVersion))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(OPEN_WITHDRAWAL_ATTRIBUTE, false));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verifyNoInteractions(applicationUpdateRequestViewService);
  }

  @Test
  void getIndustryCaseProcessing_exceededBanner() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    var consentBreach = new ConsentBreach();
    when(consentBreachService.findConsentBreachByApplication(applicationVersion.getApplication()))
        .thenReturn(Optional.of(consentBreach));

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(CONSENT_EXCEEDED_ATTRIBUTE, true));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verifyNoInteractions(applicationUpdateRequestViewService);
  }


  @Test
  void getIndustryCaseProcessing_noTabSelected_noExceededBanner() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(consentBreachService.findConsentBreachByApplication(applicationVersion.getApplication()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute(CONSENT_EXCEEDED_ATTRIBUTE, false));

    verify(applicationSummaryService).addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(), eq(user));

    verifyNoInteractions(applicationUpdateRequestViewService);
  }

  @Test
  void getIndustryCaseProcessing_fieldNotWithinProductionPeriodWarning() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(true);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion))
        .thenReturn(ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT);
    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpect(model().attribute("warning", ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT.getWarning()));
  }

  @Test
  void getIndustryCaseProcessing_facilityNoNotWithinProductionPeriodWarning() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    stubBaseServiceCalls(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    when(consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion))
        .thenReturn(ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT);
    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, VIEW_APPLICATION, null)))
            .with(user(user)))
        .andExpect(model().attributeDoesNotExist("warning"));
  }

  @Test
  void getIndustryCaseProcessing_withLicenceExpiringWithinDuration() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var tabParam = "?tab=%s".formatted(VIEW_APPLICATION.getAnchor());

    var licenceView = new LicenceView("Test123","25th of December 2024");
    var expiringLicences = List.of(licenceView);

    stubBaseServiceCalls(applicationVersion);
    stubConsentServiceCall(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion))
        .thenReturn(expiringLicences);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/industryCaseProcessing"))
        .andExpect(model().attribute("expiringLicences", expiringLicences));
  }

  @Test
  void getIndustryCaseProcessing_withoutLicenceExpiringWithinDuration() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var tabParam = "?tab=%s".formatted(VIEW_APPLICATION.getAnchor());

    List<LicenceView> expiringLicences = List.of();

    stubBaseServiceCalls(applicationVersion);
    stubConsentServiceCall(applicationVersion);
    stubSummaryServiceCall(applicationVersion);

    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
    // this is called in the IsMemberOfTeamTypeInterceptor
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion))
        .thenReturn(expiringLicences);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null)) + tabParam)
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/industryCaseProcessing"))
        .andExpect(model().attribute("expiringLicences", List.of()));
  }

  private void stubBaseServiceCalls(ApplicationVersion applicationVersion) {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(caseProcessingTabService.getIndustryTabsAvailableToUser(user, applicationVersion)).thenReturn(caseProcessingTabs);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user)).thenReturn(caseProcessingActionViews);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .withPrimaryOperator("Primary operator")
        .build());
    when(applicationService.isMigratedApplication(applicationVersion.getApplication())).thenReturn(true);
    when(consentService.shouldCheckProductionConsentExists(applicationVersion)).thenReturn(false);
}

  private void stubSummaryServiceCall(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject("summarySections", summarySections)
          .addObject("accordionId", applicationVersion.getId())
          .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
          .addObject("selectedApplicationVersionView", ApplicationVersionView.from(applicationVersion))
          .addObject("selectedApplicationVersionView", List.of());
      return null;
    })
        .when(applicationSummaryService)
        .addSummarySectionsAndVersionOptionsToModelAndView(eq(applicationVersion), any(ModelAndView.class), eq(user));
  }

  private void stubPaymentsServiceCall(
      Application application
  ) {
    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
        .addObject("paymentsTabPaymentSummaryViews", paymentsTabPaymentSummaryViews);
    return null;
    })
        .when(paymentsTabService)
        .addPaymentsTabContentToModelAndView(eq(application), any(ModelAndView.class));
  }

  private void stubConsentServiceCall(ApplicationVersion applicationVersion
  ) {
    doAnswer(invocation -> {
      invocation.getArgument(1, ModelAndView.class)
          .addObject("consentTabConsentSummaryView", consentTabConsentSummaryView);
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
        model().attribute("controllerUrl", ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null))),
        model().attribute("pageTitle", DUMMY_APP_REF),
        model().attribute("selectedTab", tab),
        model().attribute("actionList", caseProcessingActionViews),
        model().attribute("caseProcessingTabs", caseProcessingTabs),
        model().attribute("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType)),
        model().attribute("isMigratedApplication", true),
    };
  }

  private static Stream<Arguments> getInProgressAndSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT)),
        Arguments.of(ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.VENT)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
