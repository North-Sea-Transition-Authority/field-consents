package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_MANAGER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_OFFICER;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionEmailServiceTest {

  @Mock
  private EmailService emailService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private ApplicationSubmissionEmailService applicationSubmissionEmailService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private ApplicationVersion applicationVersion;

  private OrganisationUnitJson primaryOperator;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    primaryOperator = new OrganisationUnitJson(applicationVersion.getPrimaryOperatorOuId(), applicationVersion.getCachedPrimaryOperatorName());
  }

  @Test
  void sendNonAceApplicationSubmissionEmail_withNoCaseOfficersOrCaseManagersToNotify() {
    when(teamQueryService.getTeamRoles(TeamType.REGULATOR))
        .thenReturn(List.of());

    when(teamQueryService.getTeamMemberViews(List.of()))
        .thenReturn(List.of());

    applicationSubmissionEmailService.sendNonAceApplicationSubmissionEmail(applicationVersion);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendNonAceApplicationSubmissionEmail_withCaseOfficersAndCaseManagersToNotify() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder().withRole(Role.CASE_OFFICER).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.CASE_MANAGER).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.CASE_MANAGER).build()
    );

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.NON_ACE_APPLICATION_SUBMISSION, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR))
        .thenReturn(teamRoles);

    when(teamQueryService.getTeamMemberViews(teamRoles))
        .thenReturn(List.of(
            TEAM_MEMBER_VIEW_CASE_OFFICER,
            TEAM_MEMBER_VIEW_CASE_MANAGER_1,
            TEAM_MEMBER_VIEW_CASE_MANAGER_2
        ));

    applicationSubmissionEmailService.sendNonAceApplicationSubmissionEmail(applicationVersion);

    verify(emailService, Mockito.times(3)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();
    var domainReferences = domainReferenceCaptor.getAllValues();

    var firstEmailMergeFields = emailTemplates.get(0).getMailMergeFields();
    assertThat(firstEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    var thirdEmailMergeFields = emailTemplates.get(2).getMailMergeFields();
    assertThat(thirdEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    // verify email recipients
    assertThat(emailRecipientCaptor.getAllValues()).containsExactlyInAnyOrder(
        FieldConsentsEmailRecipient.from(CASE_OFFICER),
        FieldConsentsEmailRecipient.from(CASE_MANAGER_1),
        FieldConsentsEmailRecipient.from(CASE_MANAGER_2)
    );

    // verify domain references
    assertThat(domainReferences.get(0).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());
    assertThat(domainReferences.get(1).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());
    assertThat(domainReferences.get(2).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferences.get(0).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(1).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(2).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
