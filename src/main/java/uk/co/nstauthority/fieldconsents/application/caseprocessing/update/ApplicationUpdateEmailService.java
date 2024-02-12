package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class ApplicationUpdateEmailService {

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  public ApplicationUpdateEmailService(EmailService emailService, EnergyPortalUserService energyPortalUserService) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void sendApplicationUpdateRequestEmail(ApplicationVersion applicationVersion,
                                                Instant deadlineInstant) {
    var applicationSubmitter = ServiceUserDetail.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getSubmittedByWuaId()))
    );

    MergedTemplate mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST, applicationVersion)
        .withMailMergeField("OPERATOR_USER", applicationSubmitter.displayName())
        .withMailMergeField("REQUEST_DEADLINE", DateUtils.format(deadlineInstant, DATE_TIME))
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        applicationSubmitter,
        applicationVersion
    );
  }
}
