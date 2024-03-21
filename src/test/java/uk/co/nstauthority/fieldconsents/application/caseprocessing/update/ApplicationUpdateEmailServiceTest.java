package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.APPLICATION_UPDATE_RESPONSE_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.TECHNICAL_REVIEWER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.UPDATE_REQUESTER_USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.CASE_OFFICER_ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.ENERGY_PORTAL_USER_DTO;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateEmailServiceTest {

  @Mock
  private EmailService emailService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private Clock clock;

  private ApplicationUpdateEmailService applicationUpdateEmailService;

  private ApplicationVersion applicationVersion;

  private ApplicationUpdate applicationUpdate;

  private OrganisationUnitJson primaryOperator;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setCaseOfficerWuaId(UPDATE_REQUESTER_USER_WUA_ID);
    applicationUpdateEmailService = new ApplicationUpdateEmailService(
        emailService,
        energyPortalUserService,
        technicalReviewService,
        organisationUnitService);
    ApplicationVersion applicationVersionUpdate = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(
        ApplicationType.PRODUCTION, 2, 2
    );
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    applicationUpdate = ApplicationUpdateTestUtil.getClosedApplicationUpdate(
        applicationVersion,
        applicationVersionUpdate,
        ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY,
        APPLICATION_UPDATE_RESPONSE_TEXT,
        clock
    );
    primaryOperator = new OrganisationUnitJson(applicationVersion.getPrimaryOperatorOuId(), applicationVersion.getCachedPrimaryOperatorName());
  }

  @Test
  void sendApplicationUpdateRequestEmail_whenCaseOfficerRequestsAnUpdate_thenAnEmailIsSentToTheOperatorOnly() {
    var serviceDetailSubmitter = ServiceUserDetail.from(ENERGY_PORTAL_USER_DTO);

    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(energyPortalUserService.getByWuaId(any())).thenReturn(ENERGY_PORTAL_USER_DTO);

    applicationUpdateEmailService.sendApplicationUpdateRequestEmail(applicationUpdate);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(serviceDetailSubmitter).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationUpdateRequestEmail_whenUserDifferentFromCaseOfficerRequestsAnUpdate_thenAnEmailIsSentToTheOperatorAndCaseOfficer() {
    var serviceDetailSubmitter = ServiceUserDetail.from(ENERGY_PORTAL_USER_DTO);
    var serviceDetailCaseOfficer = ServiceUserDetail.from(CASE_OFFICER_ENERGY_PORTAL_USER_DTO);

    applicationUpdate.setRequestedByWuaId(TECHNICAL_REVIEWER_WUA_ID);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(energyPortalUserService.getByWuaId(any())).thenReturn(ENERGY_PORTAL_USER_DTO);

    when(energyPortalUserService.getEnergyPortalUserMap(
        List.of(
            WebUserAccountId.from(TECHNICAL_REVIEWER_WUA_ID),
            WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId())))
    ).thenReturn(
        Map.of(
            WebUserAccountId.from(TECHNICAL_REVIEWER_WUA_ID), TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO,
            WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId()), CASE_OFFICER_EPU
        ));

    applicationUpdateEmailService.sendApplicationUpdateRequestEmail(applicationUpdate);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService, Mockito.times(2)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();

    var firstEmailMergeFields = emailTemplates.get(0).getMailMergeFields();
    assertThat(firstEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME))
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME)),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name()),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO.displayName())
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(serviceDetailSubmitter).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(serviceDetailCaseOfficer).getEmailAddress());

    // verify domain reference
    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationUpdateRequestEmail_whenUserRequestsAnUpdateAndCaseOfficerHasReleasedOwnership_thenAnEmailIsSentToTheOperatorOnly() {
    var serviceDetailSubmitter = ServiceUserDetail.from(ENERGY_PORTAL_USER_DTO);

    applicationVersion.setCaseOfficerWuaId(null);
    applicationUpdate.setRequestedByWuaId(TECHNICAL_REVIEWER_WUA_ID);

    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(energyPortalUserService.getByWuaId(any())).thenReturn(ENERGY_PORTAL_USER_DTO);

    applicationUpdateEmailService.sendApplicationUpdateRequestEmail(applicationUpdate);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(serviceDetailSubmitter).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationUpdateResponseEmail_withNoEmailRecipientsToNotify() {
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.empty());
    when(energyPortalUserService.findByWuaIds(anyList())).thenReturn(Collections.emptyList());

    applicationUpdateEmailService.sendApplicationUpdateResponseEmail(applicationUpdate);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendApplicationUpdateResponseEmail_withNoOpenTechnicalReviewAndCaseOfficerOnlyNotified() {
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.empty());
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(UPDATE_REQUESTER_USER_WUA_ID))))
        .thenReturn(List.of(UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO));

    applicationUpdateEmailService.sendApplicationUpdateResponseEmail(applicationUpdate);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationUpdateResponseEmail_withOpenTechnicalReviewAndBothCaseOfficerAndTechnicalReviewerNotified() {
    var technicalReview = new TechnicalReview();
    technicalReview.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WUA_ID);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.of(technicalReview));
    when(energyPortalUserService.findByWuaIds(
        List.of(WebUserAccountId.from(UPDATE_REQUESTER_USER_WUA_ID), WebUserAccountId.from(TECHNICAL_REVIEWER_WUA_ID))))
        .thenReturn(List.of(UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO, TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO));

    applicationUpdateEmailService.sendApplicationUpdateResponseEmail(applicationUpdate);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService, Mockito.times(2)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();

    var firstEmailMergeFields = emailTemplates.get(0).getMailMergeFields();
    assertThat(firstEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO).getEmailAddress());

    // verify domain reference
    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationUpdateResponseEmail_whenTechnicalReviewHasRequestedTheUpdateAndCaseOfficerHasReleasedOwnership_thenAnEmailIsSentToTheOperatorOnly() {
    var technicalReview = new TechnicalReview();
    technicalReview.setTechnicalReviewerWuaId(2L);
    applicationVersion.setCaseOfficerWuaId(null);
    applicationUpdate.setRequestedByWuaId(TECHNICAL_REVIEWER_WUA_ID);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(technicalReviewService.findOpenTechnicalReview(applicationVersion)).thenReturn(Optional.of(technicalReview));
    when(energyPortalUserService.findByWuaIds(List.of(WebUserAccountId.from(TECHNICAL_REVIEWER_WUA_ID))))
        .thenReturn(List.of(TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO));

    applicationUpdateEmailService.sendApplicationUpdateResponseEmail(applicationUpdate);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
