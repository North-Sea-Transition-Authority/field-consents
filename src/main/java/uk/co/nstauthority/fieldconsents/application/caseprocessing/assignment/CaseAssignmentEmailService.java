package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;

@Service
public class CaseAssignmentEmailService {

  private final EmailService emailService;

  @Autowired
  public CaseAssignmentEmailService(EmailService emailService) {
    this.emailService = emailService;
  }

  public void sendCaseAssignmentEmail(ApplicationVersion applicationVersion,
                                      ServiceUserDetail caseOfficerUser,
                                      ServiceUserDetail actionUser) {

    MergedTemplate mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER, applicationVersion)
        .withMailMergeField("CASE_OFFICER", caseOfficerUser.displayName())
        .withMailMergeField("CASE_ASSIGNEE", actionUser.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        caseOfficerUser,
        applicationVersion
    );
  }
}
