package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class ApplicationUpdateEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for application update response notification";

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

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_REQUEST, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, applicationSubmitter.displayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME,
            DateUtils.format(applicationUpdate.getDeadlineDateTime(), DATE_TIME))
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(applicationSubmitter),
        applicationVersion
    );
  }

  public void sendApplicationUpdateResponseEmail(ApplicationUpdate applicationUpdate) {
    var applicationVersion = applicationUpdate.getApplicationVersion();

    var primaryOperator = organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE,
        applicationVersion.getCachedPrimaryOperatorName());

    var mergedTemplateBuilder = emailService
        .getTemplate(GovukNotifyTemplate.APPLICATION_UPDATE_RESPONSE, applicationVersion)
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

  private List<FieldConsentsEmailRecipient> getApplicationUpdateResponseEmailRecipients(ApplicationUpdate applicationUpdate) {
    var applicationVersion = applicationUpdate.getApplicationVersion();
    var emailRecipientWuaIds = new HashSet<Long>();

    var updateRequestedByWuaId = applicationUpdate.getRequestedByWuaId();
    var caseOfficerWuaId = applicationVersion.getCaseOfficerWuaId();
    emailRecipientWuaIds.add(updateRequestedByWuaId);
    emailRecipientWuaIds.add(caseOfficerWuaId);

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
