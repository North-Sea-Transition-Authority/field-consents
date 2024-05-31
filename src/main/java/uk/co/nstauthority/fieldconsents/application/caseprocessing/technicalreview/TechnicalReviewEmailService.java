package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    // Notify the technical reviewer unless a technical reviewer has reassigned the case to themselves
    if (!requesterUser.wuaId().equals(technicalReview.getTechnicalReviewerWuaId())) {
      var technicalReviewer = energyPortalUserService
          .getByWuaId(WebUserAccountId.from(technicalReview.getTechnicalReviewerWuaId()));

      var mergedTemplate = emailService
          .getTemplateForApplication(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST, applicationVersion)
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, technicalReviewer.displayName())
          .withMailMergeField(REQUESTER_USER_MERGE_FIELD_NAME, requesterUser.displayName())
          .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME,
              DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
          .merge();

      emailService.sendEmail(
          mergedTemplate,
          FieldConsentsEmailRecipient.from(technicalReviewer),
          applicationVersion
      );
    }

    // If the case officer is assigned, and they weren't the technical review requester,
    // notify the case officer about the technical review reassignment.
    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())
        && !requesterUser.wuaId().equals(applicationVersion.getCaseOfficerWuaId())) {
      sendTechnicalReviewReassignmentEmailToCaseOfficer(technicalReview, requesterUser);
    }
  }

  private void sendTechnicalReviewReassignmentEmailToCaseOfficer(TechnicalReview technicalReview,
                                                                 ServiceUserDetail requesterUser) {
    var applicationVersion = technicalReview.getRequestApplicationVersion();
    var requestedByWuaId = WebUserAccountId.from(requesterUser.wuaId());
    var technicalReviewerWuaId = WebUserAccountId.from(technicalReview.getTechnicalReviewerWuaId());
    var caseOfficerWuaId = WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId());
    var webUserAccountIds = Stream.of(requestedByWuaId, technicalReviewerWuaId, caseOfficerWuaId)
        .collect(Collectors.toSet());

    var energyPortalUserDtos = energyPortalUserService.getEnergyPortalUserMap(webUserAccountIds);

    var requestedByDto = energyPortalUserDtos.get(requestedByWuaId);
    var technicalReviewerDto = energyPortalUserDtos.get(technicalReviewerWuaId);
    var caseOfficerDto = energyPortalUserDtos.get(caseOfficerWuaId);
    var mergedCaseOfficerTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.TECHNICAL_REVIEW_REQUEST_CASE_OFFICER, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerDto.displayName())
        .withMailMergeField(REQUEST_DEADLINE_MERGE_FIELD_NAME,
            DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        .withMailMergeField(REQUESTER_USER_MERGE_FIELD_NAME, requestedByDto.displayName())
        .withMailMergeField("TECHNICAL_REVIEWER", technicalReviewerDto.displayName())
        .merge();

    emailService.sendEmail(
        mergedCaseOfficerTemplate,
        FieldConsentsEmailRecipient.from(caseOfficerDto),
        applicationVersion
    );
  }

  public void sendTechnicalReviewResponseEmail(TechnicalReview technicalReview,
                                               ServiceUserDetail technicalReviewer) {
    var applicationVersion = technicalReview.getRequestApplicationVersion();

    var requesterUser = energyPortalUserService
        .getByWuaId(WebUserAccountId.from(technicalReview.getRequestedByWuaId()));

    var mergedTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.TECHNICAL_REVIEW_RESPONSE, applicationVersion)
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
