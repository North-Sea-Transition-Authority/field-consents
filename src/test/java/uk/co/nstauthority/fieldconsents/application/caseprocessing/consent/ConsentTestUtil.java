package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class ConsentTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1;
    private Application application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    private Long issuedByWuaId = 2L;
    private Instant issuedInstant = Instant.now();

    private Builder() {
    }

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withApplication(Application application) {
      this.application = application;
      return this;
    }

    public Builder withIssuedByWuaId(Long issuedByWuaId) {
      this.issuedByWuaId = issuedByWuaId;
      return this;
    }

    public Builder withIssuedInstant(Instant issuedInstant) {
      this.issuedInstant = issuedInstant;
      return this;
    }

    public Consent build() {
      var consent = new Consent(id);

      consent.setApplication(application);
      consent.setIssuedByWuaId(issuedByWuaId);
      consent.setIssuedInstant(issuedInstant);

      return consent;
    }
  }
}
