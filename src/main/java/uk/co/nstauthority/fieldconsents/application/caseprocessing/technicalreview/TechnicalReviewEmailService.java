package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class TechnicalReviewEmailService {

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;

  TechnicalReviewEmailService(EmailService emailService, EnergyPortalUserService energyPortalUserService) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void sendTechnicalReviewRequestEmail(TechnicalReview technicalReview,
                                              ServiceUserDetail requesterUser) {
    var applicationVersion = technicalReview.getRequestApplicationVersion();

    var technicalReviewer = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(technicalReview.getTechnicalReviewerWuaId()));

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, technicalReviewer.displayName())
        .withMailMergeField("REQUESTER_USER", requesterUser.displayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME,
            DateUtils.format(technicalReview.getRequestedDateTime(), DATE_TIME))
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(technicalReviewer),
        applicationVersion
    );
  }

  public void sendTechnicalReviewResponseEmail(TechnicalReview technicalReview,
                                               ServiceUserDetail technicalReviewer) {
    var applicationVersion = technicalReview.getRequestApplicationVersion();

    var requesterUser = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(technicalReview.getRequestedByWuaId()));

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.TECHNICAL_REVIEW_RESPONSE, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, requesterUser.displayName())
        .withMailMergeField("RESPONDER_USER", technicalReviewer.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(requesterUser),
        applicationVersion
    );
  }
}
