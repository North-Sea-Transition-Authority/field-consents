package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
class ConsultationEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsultationEmailService.class);

  static final String CONSULTATION_AGREE_DECISION = "agrees";
  static final String CONSULTATION_DOES_NOT_AGREE_DECISION = "does not agree";
  static final String CASE_MANAGERS_RECIPIENT_DISPLAY_NAME = "Case Managers";

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TeamQueryService teamQueryService;

  ConsultationEmailService(
      EmailService emailService,
      EnergyPortalUserService energyPortalUserService,
      TeamQueryService teamQueryService
  ) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
    this.teamQueryService = teamQueryService;
  }

  public void sendConsultationRequestEmail(Consultation consultation) {
    var allocatorTeamRoles = teamQueryService.getTeamRoles(TeamType.CONSULTEE)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.ALLOCATOR)
        .toList();

    var emailRecipients = teamQueryService.getTeamMemberViews(allocatorTeamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());

    var emailTemplate = GovukNotifyTemplate.CONSULTATION_REQUEST;

    if (emailRecipients.isEmpty()) {
      LOGGER.info("Didn't find any consultee allocators to send [{}] email to", emailTemplate);
      return;
    }

    var applicationVersion = consultation.getRequestApplicationVersion();

    var mergedTemplate = emailService
        .getTemplateForApplication(emailTemplate, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, consultation.getConsultationTeam().getName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME, DateUtils.format(consultation.getRequestDeadline(), DATE_TIME))
        .merge();

    emailRecipients.forEach(emailRecipient -> emailService.sendEmail(mergedTemplate, emailRecipient, applicationVersion));
  }

  public void sendConsultationAssignmentEmail(Consultation consultation, ServiceUserDetail consulteeAllocator) {
    var applicationVersion = consultation.getRequestApplicationVersion();

    var consulteeResponder = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(consultation.getResponderWuaId()));

    var mergedTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.CONSULTATION_ASSIGNMENT, applicationVersion)
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
        .getTemplateForApplication(GovukNotifyTemplate.CONSULTATION_RESPONSE, applicationVersion)
        .withMailMergeField("CONSULTEE_NAME", consultation.getConsultationTeam().getName())
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

    var caseManagerTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CASE_MANAGER)
        .toList();

    var mergedTemplate = mergedTemplateBuilder
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME)
        .merge();

    // otherwise email all case managers
    teamQueryService.getTeamMemberViews(caseManagerTeamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .forEach(emailRecipient -> emailService.sendEmail(mergedTemplate, emailRecipient, applicationVersion));
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
