package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class ApplicationUpdateEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for application update notification";

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TechnicalReviewService technicalReviewService;
  private final OrganisationUnitService organisationUnitService;

  @Autowired
  ApplicationUpdateEmailService(EmailService emailService,
                                EnergyPortalUserService energyPortalUserService,
                                TechnicalReviewService technicalReviewService,
                                OrganisationUnitService organisationUnitService) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
    this.technicalReviewService = technicalReviewService;
    this.organisationUnitService = organisationUnitService;
  }

  public void sendApplicationUpdateRequestEmail(ApplicationUpdate applicationUpdate) {
    var applicationVersion = applicationUpdate.getApplicationVersion();
    var applicationSubmitter = ServiceUserDetail.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getSubmittedByWuaId()))
    );

    var mergedOperatorTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST_OPERATOR, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, applicationSubmitter.displayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME,
            DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME))
        .merge();

    emailService.sendEmail(
        mergedOperatorTemplate,
        FieldConsentsEmailRecipient.from(applicationSubmitter),
        applicationVersion
    );

    // If the case officer is the current owner of the application and the update request was submitted by a user
    // different from the assigned case officer, notify the case officer about the update request.
    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())
        && !applicationUpdate.getRequestedByWuaId().equals(applicationVersion.getCaseOfficerWuaId())) {
      sendApplicationUpdateRequestEmailToCaseOfficer(applicationUpdate);
    }
  }

  private void sendApplicationUpdateRequestEmailToCaseOfficer(ApplicationUpdate applicationUpdate) {
    var applicationVersion = applicationUpdate.getApplicationVersion();
    var primaryOperator = getApplicationPrimaryOperator(applicationVersion);

    var updateRequestedByWuaId = WebUserAccountId.from(applicationUpdate.getRequestedByWuaId());
    var caseOfficerWuaId = WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId());
    var applicationUpdateUserWuaIds = List.of(updateRequestedByWuaId, caseOfficerWuaId);

    var applicationUpdatePortalUserDtos = energyPortalUserService.getEnergyPortalUserMap(applicationUpdateUserWuaIds);

    var caseOfficerDto = applicationUpdatePortalUserDtos.get(caseOfficerWuaId);
    var updateRequestedByDto = applicationUpdatePortalUserDtos.get(updateRequestedByWuaId);
    var mergedCaseOfficerTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST_CASE_OFFICER, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerDto.displayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME,
            DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME))
        .withMailMergeField("PRIMARY_OPERATOR_NAME", primaryOperator.name())
        .withMailMergeField(REQUESTER_USER_MERGE_FIELD_NAME, updateRequestedByDto.displayName())
        .merge();

    emailService.sendEmail(
        mergedCaseOfficerTemplate,
        FieldConsentsEmailRecipient.from(caseOfficerDto),
        applicationVersion
    );
  }

  public void sendApplicationUpdateResponseEmail(ApplicationUpdate applicationUpdate) {
    var applicationVersion = applicationUpdate.getApplicationVersion();

    var primaryOperator = getApplicationPrimaryOperator(applicationVersion);

    var mergedTemplateBuilder = emailService
        .getTemplateForApplication(GovukNotifyTemplate.APPLICATION_UPDATE_RESPONSE, applicationVersion)
        .withMailMergeField("PRIMARY_OPERATOR_NAME", primaryOperator.name());

    var emailRecipients = getApplicationUpdateResponseEmailRecipients(applicationUpdate);

    // iterate over the list of email recipients to notify about the application update response
    emailRecipients.forEach(recipient -> {
      var mergedTemplate = mergedTemplateBuilder
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, recipient.displayName())
          .merge();

      emailService.sendEmail(
          mergedTemplate,
          recipient,
          applicationVersion
      );
    });
  }

  private OrganisationUnitJson getApplicationPrimaryOperator(ApplicationVersion applicationVersion) {
    return organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE,
        applicationVersion.getCachedPrimaryOperatorName());
  }

  private List<FieldConsentsEmailRecipient> getApplicationUpdateResponseEmailRecipients(ApplicationUpdate applicationUpdate) {
    var applicationVersion = applicationUpdate.getApplicationVersion();
    var emailRecipientWuaIds = new HashSet<Long>();

    emailRecipientWuaIds.add(applicationUpdate.getRequestedByWuaId());

    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())) {
      emailRecipientWuaIds.add(applicationVersion.getCaseOfficerWuaId());
    }

    technicalReviewService.findOpenTechnicalReview(applicationVersion)
        .ifPresent(technicalReview -> emailRecipientWuaIds.add(technicalReview.getTechnicalReviewerWuaId()));

    var emailRecipientWebUserAccountIds = emailRecipientWuaIds.stream()
        .map(WebUserAccountId::from)
        .toList();

    return energyPortalUserService.findByWuaIds(emailRecipientWebUserAccountIds).stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();
  }
}
