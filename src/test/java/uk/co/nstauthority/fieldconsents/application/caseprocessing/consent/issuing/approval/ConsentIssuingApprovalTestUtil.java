package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class ConsentIssuingApprovalTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Application application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    private long approvedByWuaId = 1L;
    private Instant approvedInstant = Instant.now();

    public Builder withApplication(Application application) {
      this.application = application;
      return this;
    }

    public Builder withApprovedByWuaId(long approvedByWuaId) {
      this.approvedByWuaId = approvedByWuaId;
      return this;
    }

    public Builder withApprovedInstant(Instant approvedInstant) {
      this.approvedInstant = approvedInstant;
      return this;
    }

    public ConsentIssuingApproval build() {
      var consentIssuingApproval = new ConsentIssuingApproval();

      consentIssuingApproval.setApplication(application);
      consentIssuingApproval.setApprovedByWuaId(approvedByWuaId);
      consentIssuingApproval.setApprovedInstant(approvedInstant);

      return consentIssuingApproval;
    }
  }
}
