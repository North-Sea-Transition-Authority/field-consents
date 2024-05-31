package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class TeamMemberRoleEmailServiceTest {

  @Mock
  private EmailService emailService;

  @Mock
  private AbsoluteUrlService absoluteUrlService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private TeamMemberRoleEmailService teamMemberRoleEmailService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    teamMemberRoleEmailService = new TeamMemberRoleEmailService(emailService, absoluteUrlService);
  }

  @Test
  void sendUserAddedToTeamEmail() {
    var team = new TeamTestUtil.TeamBuilder()
        .withId(1)
        .withDisplayName("Test Team")
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var newUser = ServiceUserDetailTestUtil.Builder().build();
    var workAreaUrl = "/workarea_url";
    var teamDomainReference = "TEAM";

    when(emailService.getTemplate(GovukNotifyTemplate.USER_ADDED_TO_INDUSTRY_TEAM))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(workAreaUrl);

    teamMemberRoleEmailService.sendUserAddedToTeamEmail(FieldConsentsEmailRecipient.from(newUser), team);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple("TEAM_NAME", team.getDisplayName()),
            tuple("WORK_AREA_URL", workAreaUrl),
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, newUser.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(newUser).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(teamDomainReference);
  }
}
