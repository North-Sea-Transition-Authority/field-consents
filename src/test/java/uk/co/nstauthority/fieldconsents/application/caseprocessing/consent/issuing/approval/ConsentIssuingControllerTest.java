package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

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
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentIssuingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceSummaryViewTestUtil;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@ContextConfiguration(classes = ConsentIssuingController.class)
class ConsentIssuingControllerTest extends AbstractApplicationControllerTest {

  private static final int APPLICATION_ID = 1;

  @MockBean
  private ConsentPreparationDocumentService consentPreparationDocumentService;

  @MockBean
  private ConsentIssuingApprovalService consentIssuingApprovalService;

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
  void getConsentIssuing_userDoesNotHaveConsentIssuingCaseProcessingAction() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getConsentIssuing_consentIssuingApprovalSummaryViewIsNull() throws Exception {
    var actionList = List.of(CaseProcessingActionView.from(CaseProcessingActionItem.APPROVE_FOR_ISSUING, applicationVersion));

    var documentsInstanceSummaryView = DocumentInstanceSummaryViewTestUtil.newBuilder().build();
    var consentDocumentsSummaryCard = SummaryCard.filesSummaryCardWithHeading(
        "Consent documents",
        List.of(SummaryFileView.previewSummaryFrom(documentsInstanceSummaryView))
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.CONSENT_ISSUING));
    when(
        caseProcessingActionService.getUserActionViewsForGroup(
            applicationVersion,
            user,
            CaseProcessingActionGroup.CONSENT_ISSUING
        )
    ).thenReturn(actionList);
    when(consentPreparationDocumentService.getConsentDocumentsSummaryCard(application)).thenReturn(consentDocumentsSummaryCard);
    when(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentIssuing"))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(model().attribute("actionList", actionList))
        .andExpect(model().attribute("consentDocumentsSummaryCard", consentDocumentsSummaryCard))
        .andExpect(model().attributeDoesNotExist("consentIssuingApprovalSummaryView"));
  }

  @Test
  void getConsentIssuing_consentIssuingApprovalSummaryViewIsNotNull() throws Exception {
    var actionList = List.of(CaseProcessingActionView.from(CaseProcessingActionItem.APPROVE_FOR_ISSUING, applicationVersion));

    var documentsInstanceSummaryView = DocumentInstanceSummaryViewTestUtil.newBuilder().build();
    var consentDocumentsSummaryCard = SummaryCard.filesSummaryCardWithHeading(
        "Consent documents",
        List.of(SummaryFileView.previewSummaryFrom(documentsInstanceSummaryView))
    );

    var consentIssuingApprovalSummaryView =
        new ConsentIssuingApprovalSummaryView("Test user (test@test.com)", "6 Mar 2024 11:18");

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.CONSENT_ISSUING));
    when(
        caseProcessingActionService.getUserActionViewsForGroup(
            applicationVersion,
            user,
            CaseProcessingActionGroup.CONSENT_ISSUING
        )
    ).thenReturn(actionList);
    when(consentPreparationDocumentService.getConsentDocumentsSummaryCard(application)).thenReturn(consentDocumentsSummaryCard);
    when(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application))
        .thenReturn(Optional.of(consentIssuingApprovalSummaryView));

    mockMvc.perform(get(ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentIssuing"))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(model().attribute("actionList", actionList))
        .andExpect(model().attribute("consentDocumentsSummaryCard", consentDocumentsSummaryCard))
        .andExpect(model().attribute("consentIssuingApprovalSummaryView", consentIssuingApprovalSummaryView));
  }

  @SecurityTest
  void approveForIssuing_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).approveForIssuing(APPLICATION_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void approveForIssuing_userDoesNotHaveApproveForIssuingCaseProcessingAction() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ConsentIssuingController.class).approveForIssuing(APPLICATION_ID, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void approveForIssuing() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.APPROVE_FOR_ISSUING));

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
}
