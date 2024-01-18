package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class ConsentDataTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1;
    private Application application = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION).getApplication();
    private LocalDate consentStartDate = LocalDate.parse("2024-01-01");
    private LocalDate consentEndDate = consentStartDate.plusYears(1);

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withApplication(Application application) {
      this.application = application;
      return this;
    }

    public Builder withConsentStartDate(LocalDate consentStartDate) {
      this.consentStartDate = consentStartDate;
      return this;
    }

    public Builder withConsentEndDate(LocalDate consentEndDate) {
      this.consentEndDate = consentEndDate;
      return this;
    }

    public ConsentData build() {
      var consentData = new ConsentData(id);
      consentData.setApplication(application);
      consentData.setConsentStartDate(consentStartDate);
      consentData.setConsentEndDate(consentEndDate);

      return consentData;
    }

  }

}
