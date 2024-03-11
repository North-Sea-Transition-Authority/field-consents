package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole;

@Service
public class ConsultationEmailService {

  private final EmailService emailService;
  private final TeamMemberViewService teamMemberViewService;
  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  public ConsultationEmailService(EmailService emailService,
                                  TeamMemberViewService teamMemberViewService,
                                  EnergyPortalUserService energyPortalUserService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void sendConsultationRequestEmail(Consultation consultation) {
    var applicationVersion = consultation.getRequestApplicationVersion();

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TeamType.OPRED.getDisplayText())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(consultation.getRequestDeadline(), DATE_TIME))
        .merge();

    var opredAllocatorEmailRecipients = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.OPRED, Set.of(OpredTeamRole.ALLOCATOR))
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();

    // iterate over the list of OPRED allocators to send an email out to each recipient
    opredAllocatorEmailRecipients.forEach(opredAllocator -> {
      emailService.sendEmail(
          mergedTemplate,
          opredAllocator,
          applicationVersion
      );
    });
  }

  public void sendConsultationAssignmentEmail(Consultation consultation, ServiceUserDetail consulteeAllocator) {
    var applicationVersion = consultation.getRequestApplicationVersion();

    var consulteeResponder = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(consultation.getResponderWuaId()));

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.CONSULTATION_ASSIGNMENT, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, consulteeResponder.displayName())
        .withMailMergeField(REQUESTER_USER_MERGE_FIELD_NAME, consulteeAllocator.displayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(consultation.getRequestDeadline(), DATE_TIME))
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(consulteeResponder),
        applicationVersion
    );
  }
}
