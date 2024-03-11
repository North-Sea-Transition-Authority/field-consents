package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record ConsentIssuingApprovalSummaryView(
    String formattedApprovedByUser,
    String formattedApprovedDate
) {

  public static ConsentIssuingApprovalSummaryView from(
      ConsentIssuingApproval consentIssuingApproval,
      ServiceUserDetail approvedByUser
  ) {
    var formattedApprovedByUser = approvedByUser.displayNameAndEmail();
    var formattedApprovedDate = DateUtils.format(consentIssuingApproval.getApprovedInstant(), DateUtils.DATE_TIME);

    return new ConsentIssuingApprovalSummaryView(formattedApprovedByUser, formattedApprovedDate);
  }
}
