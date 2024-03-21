package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_USER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_EPU_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_EPU_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_USER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.time.Clock;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewEmailServiceTest {

  @Mock
  private Clock clock;

  @Mock
  private EmailService emailService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private TechnicalReviewEmailService technicalReviewEmailService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    applicationVersion.setCaseOfficerWuaId(CASE_OFFICER_USER.wuaId());
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, TECHNICAL_REVIEWER_USER_1, clock);
    technicalReviewEmailService = new TechnicalReviewEmailService(emailService, energyPortalUserService);
  }

  @Test
  void sendTechnicalReviewRequestEmail_whenCaseOfficerAssignsTechnicalReviewer_thenEmailAssignedTechnicalReviewerOnly() {
    when(energyPortalUserService.getByWuaId(any())).thenReturn(TECHNICAL_REVIEWER_EPU_1);
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, CASE_OFFICER_USER);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_USER_1.displayName()),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, FieldConsentsEmailRecipient.from(CASE_OFFICER_USER).displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendTechnicalReviewRequestEmail_whenTechnicalReviewerReassignsTechnicalReviewAndCaseOfficerStillAssigned_thenEmailAssignedTechnicalReviewerAndCaseOfficer() {
    technicalReview.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_USER_2.wuaId());

    when(energyPortalUserService.getByWuaId(any())).thenReturn(TECHNICAL_REVIEWER_EPU_2);
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(energyPortalUserService.getEnergyPortalUserMap(
        Set.of(
            WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId()),
            WebUserAccountId.from(TECHNICAL_REVIEWER_USER_1),
            WebUserAccountId.from(TECHNICAL_REVIEWER_USER_2)))
    ).thenReturn(
        Map.of(
            WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId()), CASE_OFFICER_EPU,
            WebUserAccountId.from(TECHNICAL_REVIEWER_USER_1), TECHNICAL_REVIEWER_EPU_1,
            WebUserAccountId.from(TECHNICAL_REVIEWER_USER_2), TECHNICAL_REVIEWER_EPU_2
        ));

    technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, TECHNICAL_REVIEWER_USER_1);

    verify(emailService, Mockito.times(2)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();

    var technicalReviewerEmailMergeFields = emailTemplates.get(0).getMailMergeFields();
    assertThat(technicalReviewerEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_USER_2.displayName()),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        );

    var caseOfficerEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(caseOfficerEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME)),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).displayName()),
            tuple("TECHNICAL_REVIEWER", FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_2).displayName())
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_USER).getEmailAddress());

    // verify domain reference
    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendTechnicalReviewRequestEmail_whenTechnicalReviewerReassignsTechnicalReviewAndCaseOfficerHasReleasedOwnership_thenEmailAssignedTechnicalReviewerOnly() {
    technicalReview.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_USER_2.wuaId());
    applicationVersion.setCaseOfficerWuaId(null);

    when(energyPortalUserService.getByWuaId(any())).thenReturn(TECHNICAL_REVIEWER_EPU_2);
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, TECHNICAL_REVIEWER_USER_1);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_USER_2.displayName()),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo("APPLICATION_VERSION");
  }

  @Test
  void sendTechnicalReviewRequestEmail_whenTechnicalReviewerReassignsToThemselvesAndCaseOfficerStillAssigned_thenEmailCaseOfficerOnly() {
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(energyPortalUserService.getEnergyPortalUserMap(
        Set.of(
            WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId()),
            WebUserAccountId.from(TECHNICAL_REVIEWER_USER_1)))
    ).thenReturn(
        Map.of(
            WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId()), CASE_OFFICER_EPU,
            WebUserAccountId.from(TECHNICAL_REVIEWER_USER_1), TECHNICAL_REVIEWER_EPU_1
        ));

    technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, TECHNICAL_REVIEWER_USER_1);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME)),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).displayName()),
            tuple("TECHNICAL_REVIEWER", FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendTechnicalReviewRequestEmail_whenTechnicalReviewerReassignsToThemselvesAndCaseOfficerHasReleasedOwnership_thenNoEmailIsSentOut() {
    applicationVersion.setCaseOfficerWuaId(null);

    technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, TECHNICAL_REVIEWER_USER_1);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendTechnicalReviewResponseEmail() {
    when(energyPortalUserService.getByWuaId(any())).thenReturn(CASE_OFFICER_EPU);
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    technicalReviewEmailService.sendTechnicalReviewResponseEmail(technicalReview, TECHNICAL_REVIEWER_USER_1);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_USER.displayName()),
            tuple("RESPONDER_USER", FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER_1).displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
