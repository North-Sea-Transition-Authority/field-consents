package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class ConsentDataLongTermProductionFiguresTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1;
    private Application application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    private Integer year = 2024;
    private BigDecimal minOil = BigDecimal.valueOf(235.79);
    private BigDecimal maxOil = BigDecimal.valueOf(673.12);
    private BigDecimal minGas = BigDecimal.valueOf(112.89);
    private BigDecimal maxGas = BigDecimal.valueOf(456.99);

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

    public Builder withMinOil(BigDecimal minOil) {
      this.minOil = minOil;
      return this;
    }

    public Builder withMaxOil(BigDecimal maxOil) {
      this.maxOil = maxOil;
      return this;
    }

    public Builder withMinGas(BigDecimal minGas) {
      this.minGas = minGas;
      return this;
    }

    public Builder withMaxGas(BigDecimal maxGas) {
      this.maxGas = maxGas;
      return this;
    }

    public ConsentDataLongTermProductionFigures build() {
      var consentDataLongTermProductionFigures = new ConsentDataLongTermProductionFigures(id);

      consentDataLongTermProductionFigures.setApplication(application);
      consentDataLongTermProductionFigures.setYear(year);
      consentDataLongTermProductionFigures.setMinOil(minOil);
      consentDataLongTermProductionFigures.setMaxOil(maxOil);
      consentDataLongTermProductionFigures.setMinGas(minGas);
      consentDataLongTermProductionFigures.setMaxGas(maxGas);

      return consentDataLongTermProductionFigures;
    }
  }
}
