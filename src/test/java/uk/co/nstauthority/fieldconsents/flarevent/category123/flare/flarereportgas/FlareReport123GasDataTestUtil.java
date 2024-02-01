package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class FlareReport123GasDataTestUtil {

  private static final Double CATEGORY_1_DENSITY = 12.4;
  private static final Double CATEGORY_1_INERT_GAS = 25.5;
  private static final Double CATEGORY_1_HYDROCARBON = 74.5;
  private static final Double CATEGORY_2_DENSITY = 0.745;
  private static final Double CATEGORY_2_INERT_GAS = 45.761;
  private static final Double CATEGORY_2_HYDROCARBON = 54.239;
  private static final Double CATEGORY_3_DENSITY = 123.0052;
  private static final Double CATEGORY_3_INERT_GAS = 52.0475;
  private static final Double CATEGORY_3_HYDROCARBON = 47.9525;

  static FlareReport123GasData getCompleteAndValidFlareReport123GasData(ApplicationVersion applicationVersion) {
    var flareReport123GasData = new FlareReport123GasData();

    flareReport123GasData.setApplicationVersion(applicationVersion);
    flareReport123GasData.setCategory1Density(BigDecimal.valueOf(CATEGORY_1_DENSITY));
    flareReport123GasData.setCategory1InertGasPercentage(BigDecimal.valueOf(CATEGORY_1_INERT_GAS));
    flareReport123GasData.setCategory1HydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_1_HYDROCARBON));
    flareReport123GasData.setCategory2Density(BigDecimal.valueOf(CATEGORY_2_DENSITY));
    flareReport123GasData.setCategory2InertGasPercentage(BigDecimal.valueOf(CATEGORY_2_INERT_GAS));
    flareReport123GasData.setCategory2HydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_2_HYDROCARBON));
    flareReport123GasData.setCategory3Density(BigDecimal.valueOf(CATEGORY_3_DENSITY));
    flareReport123GasData.setCategory3InertGasPercentage(BigDecimal.valueOf(CATEGORY_3_INERT_GAS));
    flareReport123GasData.setCategory3HydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_3_HYDROCARBON));

    return flareReport123GasData;
  }
}
