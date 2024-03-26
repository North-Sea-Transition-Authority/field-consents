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
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
class ConsultationEmailService {

  static final String CONSULTATION_AGREE_DECISION = "agrees";
  static final String CONSULTATION_DOES_NOT_AGREE_DECISION = "does not agree";
  static final String CASE_MANAGERS_RECIPIENT_DISPLAY_NAME = "Case Managers";

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
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, consultation.getConsultationTeam().getDisplayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(consultation.getRequestDeadline(), DATE_TIME))
        .merge();

    var allocatorEmailRecipients = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(consultation.getConsultationTeam(), Set.of(OpredTeamRole.ALLOCATOR))
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();

    // iterate over the list of allocators to send an email out to each recipient
    allocatorEmailRecipients.forEach(opredAllocator ->
        emailService.sendEmail(
            mergedTemplate,
            opredAllocator,
            applicationVersion
        ));
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

  public void sendConsultationResponseEmail(Consultation consultation) {
    var applicationVersion = consultation.getRequestApplicationVersion();

    var consultationDecision = getConsultationDecision(consultation);
    var mergedTemplateBuilder = emailService
        .getTemplate(GovukNotifyTemplate.CONSULTATION_RESPONSE, applicationVersion)
        .withMailMergeField("CONSULTEE_NAME", consultation.getConsultationTeam().getDisplayName())
        .withMailMergeField("CONSULTATION_DECISION", consultationDecision);

    // email the case officer if available
    if (applicationVersion.getCaseOfficerWuaId() != null) {
      var caseOfficerEmailRecipient = FieldConsentsEmailRecipient.from(
          energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId()))
      );

      var mergedTemplate = mergedTemplateBuilder
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerEmailRecipient.displayName())
          .merge();

      emailService.sendEmail(
          mergedTemplate,
          caseOfficerEmailRecipient,
          applicationVersion
      );

      return;
    }

    // otherwise email all case managers
    var caseManagerEmailRecipients = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER))
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();

    var mergedTemplate = mergedTemplateBuilder
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME)
        .merge();

    caseManagerEmailRecipients.forEach(caseManagerEmailRecipient ->
        emailService.sendEmail(
            mergedTemplate,
            caseManagerEmailRecipient,
            applicationVersion
        ));
  }

  String getConsultationDecision(Consultation consultation) {
    if (HabitatsRegsResponseType.DO_NOT_AGREE.equals(consultation.getHabitatsRegsResponseType())) {
      return CONSULTATION_DOES_NOT_AGREE_DECISION;
    }

    // we don't always have EiaRegsResponseType
    return EiaRegsResponseType.DO_NOT_AGREE.equals(consultation.getEiaRegsResponseType())
        ? CONSULTATION_DOES_NOT_AGREE_DECISION
        : CONSULTATION_AGREE_DECISION;
  }
}
