package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class ConsentDataLongTermEmissionFiguresTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1;
    private Application application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.FLARE);
    private Integer year = 2024;
    private BigDecimal dailyAverage = BigDecimal.valueOf(235.79);

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withApplication(Application application) {
      this.application = application;
      return this;
    }

    public Builder withYear(Integer year) {
      this.year = year;
      return this;
    }

    public Builder withDailyAverage(BigDecimal dailyAverage) {
      this.dailyAverage = dailyAverage;
      return this;
    }

    public ConsentDataLongTermEmissionFigures build() {
      var consentDataLongTermEmissionFigures = new ConsentDataLongTermEmissionFigures(id);

      consentDataLongTermEmissionFigures.setApplication(application);
      consentDataLongTermEmissionFigures.setYear(year);
      consentDataLongTermEmissionFigures.setDailyAverage(dailyAverage);

      return consentDataLongTermEmissionFigures;
    }
  }
}
