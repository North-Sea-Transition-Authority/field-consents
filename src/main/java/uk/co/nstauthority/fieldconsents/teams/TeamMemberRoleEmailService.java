package uk.co.nstauthority.fieldconsents.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Service
public class TeamMemberRoleEmailService {

  private final EmailService emailService;
  private final AbsoluteUrlService absoluteUrlService;

  @Autowired
  public TeamMemberRoleEmailService(EmailService emailService, AbsoluteUrlService absoluteUrlService) {
    this.emailService = emailService;
    this.absoluteUrlService = absoluteUrlService;
  }

  public void sendUserAddedToTeamEmail(FieldConsentsEmailRecipient userEmailRecipient,
                                       Team team) {
    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.USER_ADDED_TO_INDUSTRY_TEAM)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, userEmailRecipient.displayName())
        .withMailMergeField("TEAM_NAME", team.getDisplayName())
        .withMailMergeField("WORK_AREA_URL",
            absoluteUrlService.getAbsoluteUrl(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        userEmailRecipient,
        team
    );
  }
}
