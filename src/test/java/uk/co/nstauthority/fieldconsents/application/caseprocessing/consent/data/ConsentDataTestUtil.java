package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.math.BigDecimal;
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
    private BigDecimal shortTermOrAnnualProductionMinOil = BigDecimal.valueOf(235.79);
    private BigDecimal shortTermOrAnnualProductionMaxOil = BigDecimal.valueOf(673.12);
    private BigDecimal shortTermOrAnnualProductionMinGas = BigDecimal.valueOf(112.89);
    private BigDecimal shortTermOrAnnualProductionMaxGas = BigDecimal.valueOf(456.99);
    private BigDecimal emissionDailyAverage;

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

    public Builder withShortTermOrAnnualProductionMinOil(BigDecimal shortTermOrAnnualProductionMinOil) {
      this.shortTermOrAnnualProductionMinOil = shortTermOrAnnualProductionMinOil;
      return this;
    }

    public Builder withShortTermOrAnnualProductionMaxOil(BigDecimal shortTermOrAnnualProductionMaxOil) {
      this.shortTermOrAnnualProductionMaxOil = shortTermOrAnnualProductionMaxOil;
      return this;
    }

    public Builder withShortTermOrAnnualProductionMinGas(BigDecimal shortTermOrAnnualProductionMinGas) {
      this.shortTermOrAnnualProductionMinGas = shortTermOrAnnualProductionMinGas;
      return this;
    }

    public Builder withShortTermOrAnnualProductionMaxGas(BigDecimal shortTermOrAnnualProductionMaxGas) {
      this.shortTermOrAnnualProductionMaxGas = shortTermOrAnnualProductionMaxGas;
      return this;
    }

    public Builder withEmissionDailyAverage(BigDecimal emissionDailyAverage) {
      this.emissionDailyAverage = emissionDailyAverage;
      return this;
    }

    public ConsentData build() {
      var consentData = new ConsentData(id);
      consentData.setApplication(application);
      consentData.setConsentStartDate(consentStartDate);
      consentData.setConsentEndDate(consentEndDate);
      consentData.setShortTermOrAnnualProductionMinOil(shortTermOrAnnualProductionMinOil);
      consentData.setShortTermOrAnnualProductionMaxOil(shortTermOrAnnualProductionMaxOil);
      consentData.setShortTermOrAnnualProductionMinGas(shortTermOrAnnualProductionMinGas);
      consentData.setShortTermOrAnnualProductionMaxGas(shortTermOrAnnualProductionMaxGas);
      consentData.setEmissionDailyAverage(emissionDailyAverage);

      return consentData;
    }
  }
}
