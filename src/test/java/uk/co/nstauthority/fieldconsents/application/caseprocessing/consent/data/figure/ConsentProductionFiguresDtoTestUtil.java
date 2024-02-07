package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;

public class ConsentProductionFiguresDtoTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private BigDecimal minOil = BigDecimal.valueOf(235.79);
    private BigDecimal maxOil = BigDecimal.valueOf(673.12);
    private BigDecimal minGas = BigDecimal.valueOf(112.89);
    private BigDecimal maxGas = BigDecimal.valueOf(456.99);

    private Builder() {
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

    public ConsentProductionFiguresDto build() {
      return new ConsentProductionFiguresDto(minOil, maxOil, minGas, maxGas);
    }
  }
}
