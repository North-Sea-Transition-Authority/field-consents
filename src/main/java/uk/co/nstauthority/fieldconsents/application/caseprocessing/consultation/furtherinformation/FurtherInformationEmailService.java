package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class FurtherInformationEmailService {

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;

  public FurtherInformationEmailService(EmailService emailService, EnergyPortalUserService energyPortalUserService) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void sendFurtherInformationRequestEmail(FurtherInformation furtherInformation) {
    var applicationVersion = furtherInformation.getConsultation().getRequestApplicationVersion();
    var consultationTeam = furtherInformation.getConsultation().getConsultationTeam();
    var caseOfficer = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId()));

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.FURTHER_INFORMATION_REQUEST, applicationVersion)
        .withMailMergeField("CONSULTEE_NAME", consultationTeam.getDisplayName())
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficer.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(caseOfficer),
        applicationVersion
    );
  }

  public void sendFurtherInformationResponseEmail(FurtherInformation furtherInformation) {
    var applicationVersion = furtherInformation.getConsultation().getRequestApplicationVersion();
    var furtherInformationRequester = energyPortalUserService.getByWuaId(
        WebUserAccountId.from(furtherInformation.getRequestedByWuaId()));

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.FURTHER_INFORMATION_RESPONSE, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, furtherInformationRequester.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(furtherInformationRequester),
        applicationVersion
    );
  }
}
