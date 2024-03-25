package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

class ConsentTestUtil {

  static Builder newBuilder() {
    return new Builder();
  }

  static class Builder {

    private Integer id = 1;
    private Application application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    private Long issuedByWuaId = 2L;
    private Instant issuedInstant = Instant.now();

    private Builder() {
    }

    Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    Builder withApplication(Application application) {
      this.application = application;
      return this;
    }

    Builder withIssuedByWuaId(Long issuedByWuaId) {
      this.issuedByWuaId = issuedByWuaId;
      return this;
    }

    Builder withIssuedInstant(Instant issuedInstant) {
      this.issuedInstant = issuedInstant;
      return this;
    }

    Consent build() {
      var consent = new Consent(id);

      consent.setApplication(application);
      consent.setIssuedByWuaId(issuedByWuaId);
      consent.setIssuedInstant(issuedInstant);

      return consent;
    }
  }
}
