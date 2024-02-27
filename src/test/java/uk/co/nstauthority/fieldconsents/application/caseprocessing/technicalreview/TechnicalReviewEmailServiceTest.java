package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_USER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_EPU;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_USER;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.time.Clock;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
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

  private TechnicalReviewEmailService technicalReviewEmailService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, TECHNICAL_REVIEWER_USER, clock);
    technicalReviewEmailService = new TechnicalReviewEmailService(emailService, energyPortalUserService);
  }

  @Test
  void sendTechnicalReviewRequestEmail() {
    when(energyPortalUserService.getByWuaId(any())).thenReturn(TECHNICAL_REVIEWER_EPU);
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, CASE_OFFICER_USER);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_USER.displayName()),
            tuple("REQUESTER_USER", FieldConsentsEmailRecipient.from(CASE_OFFICER_USER).displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo("APPLICATION_VERSION");
  }

  @Test
  void sendTechnicalReviewResponseEmail() {
    when(energyPortalUserService.getByWuaId(any())).thenReturn(CASE_OFFICER_EPU);
    when(emailService.getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    technicalReviewEmailService.sendTechnicalReviewResponseEmail(technicalReview, TECHNICAL_REVIEWER_USER);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_USER.displayName()),
            tuple("RESPONDER_USER", FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_USER).displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo("APPLICATION_VERSION");
  }
}
