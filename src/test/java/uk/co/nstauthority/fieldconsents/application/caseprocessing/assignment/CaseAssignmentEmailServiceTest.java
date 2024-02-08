package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentEmailServiceTest {

  private static final ServiceUserDetail CASE_OFFICER = ServiceUserDetailTestUtil.Builder()
      .withForename("Case")
      .withSurname("Officer")
      .withEmailAddress("case.officer@email.co.uk")
      .build();

  private static final ServiceUserDetail CASE_MANAGER = ServiceUserDetailTestUtil.Builder()
      .withForename("Case")
      .withSurname("Manager")
      .withEmailAddress("case.manager@email.co.uk")
      .withWuaId(2L)
      .build();

  private static final String APPLICATION_VERSION_DOMAIN_REFERENCE = "APPLICATION_VERSION";

  @Mock
  private EmailService emailService;

  private CaseAssignmentEmailService caseAssignmentEmailService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(emailService.getTemplate(GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    caseAssignmentEmailService = new CaseAssignmentEmailService(emailService);
  }

  @ParameterizedTest
  @MethodSource("getCaseAssignmentUsersInvolved_arguments")
  void sendCaseAssignmentEmail(ServiceUserDetail caseOfficer, ServiceUserDetail assigneeUser) {
    caseAssignmentEmailService.sendCaseAssignmentEmail(applicationVersion, caseOfficer, assigneeUser);

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
            tuple("CASE_OFFICER", caseOfficer.displayName()),
            tuple("CASE_ASSIGNEE", assigneeUser.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(caseOfficer.getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  private static Stream<Arguments> getCaseAssignmentUsersInvolved_arguments() {
    return Stream.of(
        arguments(CASE_OFFICER, CASE_MANAGER),
        arguments(CASE_OFFICER, CASE_OFFICER)
    );
  }
}
