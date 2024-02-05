package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class VentReport123GasDataTestUtil {

  private static final Double CATEGORY_1_DENSITY = 12.4;
  private static final Double CATEGORY_1_INERT_GAS = 25.5;
  private static final Double CATEGORY_1_HYDROCARBON = 74.5;
  static VentReport123GasData getCompleteAndValidVentReport123GasData(ApplicationVersion applicationVersion) {
    var ventReport123GasData = new VentReport123GasData();

    ventReport123GasData.setApplicationVersion(applicationVersion);
    ventReport123GasData.setCategory1Density(BigDecimal.valueOf(CATEGORY_1_DENSITY));
    ventReport123GasData.setCategory1InertGasPercentage(BigDecimal.valueOf(CATEGORY_1_INERT_GAS));
    ventReport123GasData.setCategory1HydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_1_HYDROCARBON));

    return ventReport123GasData;
  }
}
