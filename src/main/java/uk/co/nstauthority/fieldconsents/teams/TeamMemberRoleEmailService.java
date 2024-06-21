package uk.co.nstauthority.fieldconsents.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
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
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @Autowired
  public TeamMemberRoleEmailService(EmailService emailService,
                                    AbsoluteUrlService absoluteUrlService,
                                    CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.emailService = emailService;
    this.absoluteUrlService = absoluteUrlService;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  public void sendUserAddedToTeamEmail(FieldConsentsEmailRecipient userEmailRecipient,
                                       Team team) {
    var teamName = TeamType.REGULATOR.equals(team.getTeamType())
        ? customerBrandingConfigurationProperties.mnemonic()
        : team.getDisplayName();
    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.USER_ADDED_TO_TEAM)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, userEmailRecipient.displayName())
        .withMailMergeField("TEAM_NAME", teamName)
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
