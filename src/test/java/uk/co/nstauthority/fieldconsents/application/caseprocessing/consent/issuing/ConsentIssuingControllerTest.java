package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPROVE_FOR_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.ISSUE_CONSENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.UNAPPROVE_FOR_ISSUING;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceSummaryViewTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ConsentIssuingController.class)
class ConsentIssuingControllerTest extends AbstractApplicationControllerTest {

  private static final int APPLICATION_ID = 1;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationContextService applicationContextService;

  @MockBean
  private ConsentLengthService consentLengthService;

  @MockBean
  private ConsentDataService consentDataService;

  @MockBean
  private ConsentFigureUnitService consentFigureUnitService;

  @MockBean
  private ConsentPreparationDocumentService consentPreparationDocumentService;

  @MockBean
  private ConsentIssuingApprovalService consentIssuingApprovalService;

  @MockBean
  private ConsentIssuingService consentIssuingService;

  @MockBean
  private ConsentService consentService;

  @MockBean
  private CaseStatusFlagService caseStatusFlagService;

  private ApplicationVersion applicationVersion;
  private Application application;

  @BeforeEach
  void beforeEach() {
    applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getConsentIssuing_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getConsentIssuing_userDoesNotHaveConsentIssuingCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getConsentIssuing_consentIssuingApprovalSummaryViewIsNull() throws Exception {
    var consentIssuingGroupActionViewList = List.of(CaseProcessingActionView.from(CaseProcessingActionItem.APPROVE_FOR_ISSUING, applicationVersion));

    var consentLengthType = ConsentLengthType.SHORT_TERM;
    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(ProductionUnit.KSCM_PER_DAY);

    var documentsInstanceSummaryView = DocumentInstanceSummaryViewTestUtil.newBuilder().build();
    var consentDocumentsSummaryCard = SummaryCard.filesSummaryCardWithHeading(
        "Consent documents",
        List.of(SummaryFileView.previewSummaryFrom(application, documentsInstanceSummaryView, true))
    );

    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, CaseProcessingActionItem.CONSENT_ISSUING)).thenReturn(true);
    when(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)).thenReturn(Optional.empty());
    when(
        caseProcessingActionService.getUserActionViewsForGroup(
            applicationVersion,
            user,
            CaseProcessingActionGroup.CONSENT_ISSUING
        )
    ).thenReturn(consentIssuingGroupActionViewList);
    when(consentPreparationDocumentService.getConsentDocumentsSummaryCard(application)).thenReturn(consentDocumentsSummaryCard);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentDataService.getConsentData(application)).thenReturn(consentData);
    when(consentDataService.getConsentDataView(application, consentData, consentLengthType)).thenReturn(consentDataView);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);

    when(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.MAIL_MERGE_ERROR_PRESENT))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentIssuing"))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(model().attributeDoesNotExist("consentIssuingApprovalSummaryView"))
        .andExpect(model().attribute("consentIssuingGroupActionViewList", consentIssuingGroupActionViewList))
        .andExpect(model().attribute("applicationType", application.getType()))
        .andExpect(model().attribute("consentLengthType", consentLengthType))
        .andExpect(model().attribute("consentDataView", consentDataView))
        .andExpect(model().attribute("consentFigureUnitView", consentFigureUnitView))
        .andExpect(model().attribute("consentDocumentsSummaryCard", consentDocumentsSummaryCard))
        .andExpect(model().attribute("singleErrorMessage", "Document mail merge errors are preventing this consent from being issuable"));
  }

  @SecurityTest
  void getConsentIssuing_consentIssuingApprovalSummaryViewIsNotNull() throws Exception {
    var consentIssuingApprovalSummaryView =
        new ConsentIssuingApprovalSummaryView("Test user (test@SecurityTest.com)", "6 Mar 2024 11:18");

    var consentIssuingGroupActionViewList = List.of(CaseProcessingActionView.from(CaseProcessingActionItem.APPROVE_FOR_ISSUING, applicationVersion));

    var consentLengthType = ConsentLengthType.SHORT_TERM;
    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(ProductionUnit.KSCM_PER_DAY);

    var documentsInstanceSummaryView = DocumentInstanceSummaryViewTestUtil.newBuilder().build();
    var consentDocumentsSummaryCard = SummaryCard.filesSummaryCardWithHeading(
        "Consent documents",
        List.of(SummaryFileView.previewSummaryFrom(application, documentsInstanceSummaryView, true))
    );

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CaseProcessingActionItem.CONSENT_ISSUING
    )).thenReturn(true);
    when(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application))
        .thenReturn(Optional.of(consentIssuingApprovalSummaryView));
    when(
        caseProcessingActionService.getUserActionViewsForGroup(
            applicationVersion,
            user,
            CaseProcessingActionGroup.CONSENT_ISSUING
        )
    ).thenReturn(consentIssuingGroupActionViewList);
    when(consentPreparationDocumentService.getConsentDocumentsSummaryCard(application)).thenReturn(consentDocumentsSummaryCard);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentDataService.getConsentData(application)).thenReturn(consentData);
    when(consentDataService.getConsentDataView(application, consentData, consentLengthType)).thenReturn(consentDataView);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);

    when(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.MAIL_MERGE_ERROR_PRESENT))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentIssuing"))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("consentIssuingApprovalSummaryView", consentIssuingApprovalSummaryView))
        .andExpect(model().attribute("consentIssuingGroupActionViewList", consentIssuingGroupActionViewList))
        .andExpect(model().attribute("applicationType", application.getType()))
        .andExpect(model().attribute("consentLengthType", consentLengthType))
        .andExpect(model().attribute("consentDataView", consentDataView))
        .andExpect(model().attribute("consentFigureUnitView", consentFigureUnitView))
        .andExpect(model().attribute("consentDocumentsSummaryCard", consentDocumentsSummaryCard))
        .andExpect(model().attribute("singleErrorMessage", "Document mail merge errors are preventing this consent from being issuable"));
  }

  @SecurityTest
  void approveForIssuing_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).approveForIssuing(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void approveForIssuing_userDoesNotHaveApproveForIssuingCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).approveForIssuing(APPLICATION_ID, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void approveForIssuing() throws Exception {
    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, APPROVE_FOR_ISSUING)).thenReturn(true);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Application marked as ready to grant and issue")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).approveForIssuing(APPLICATION_ID, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentIssuingController.class)
            .getConsentIssuing(APPLICATION_ID, null))));

    verify(consentIssuingApprovalService).approveApplicationForConsentIssuing(application, user);
  }

  @SecurityTest
  void getIssueConsent_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getIssueConsent(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getIssueConsent_userDoesNotHaveIssueConsentCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getIssueConsent(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getIssueConsent_applicationIsNotRevision() throws Exception {
    application.setVariationNo(0);

    var applicationReference = "Test/application/reference";
    var applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(FieldTestUtil.field1Json)
        .withPrimaryOperator("Primary operator")
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .build();

    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, ISSUE_CONSENT)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getIssueConsent(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/issueConsent"))
        .andExpect(model().attribute("pageTitle", applicationReference))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ConsentIssuingController.class)
            .getConsentIssuing(APPLICATION_ID, null))))
        .andExpect(model().attributeDoesNotExist("previousConsentApplicationReference"));
  }

  @SecurityTest
  void getIssueConsent_applicationIsRevision() throws Exception {
    application.setVariationNo(1);

    var applicationReference = "Test/application/reference";
    var applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(FieldTestUtil.field1Json)
        .withPrimaryOperator("Primary operator")
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .build();

    var previousConsent = ConsentTestUtil.newBuilder().build();
    var previousConsentApplicationReference = "Test/application/reference/2";

    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, ISSUE_CONSENT)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(consentService.getPreviousConsent(application)).thenReturn(previousConsent);
    when(consentService.generateConsentApplicationReference(previousConsent)).thenReturn(previousConsentApplicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getIssueConsent(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/issueConsent"))
        .andExpect(model().attribute("pageTitle", applicationReference))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ConsentIssuingController.class)
            .getConsentIssuing(APPLICATION_ID, null))))
        .andExpect(model().attribute("previousConsentApplicationReference", previousConsentApplicationReference));
  }

  @SecurityTest
  void unapproveForIssuing_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).unapproveForIssuing(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void unapproveForIssuing_userDoesNotHaveUnapproveForIssuingCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).unapproveForIssuing(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void unapproveForIssuing() throws Exception {
    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, UNAPPROVE_FOR_ISSUING)).thenReturn(true);
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Application unmarked as ready to grant and issue")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).unapproveForIssuing(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentIssuingController.class)
            .getConsentIssuing(APPLICATION_ID, null))));

    verify(consentIssuingApprovalService).deleteConsentIssuingApproval(application);
  }

  @SecurityTest
  void issueConsent_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).issueConsent(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void issueConsent_userDoesNotHaveIssueConsentCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).issueConsent(APPLICATION_ID, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void issueConsent() throws Exception {
    var applicationReference = "Test/application/reference";

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Consent issued for %s".formatted(applicationReference))
        .build();

    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, ISSUE_CONSENT)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).issueConsent(APPLICATION_ID, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(consentIssuingService).issueConsent(applicationVersion, user);
  }
}
