package uk.co.nstauthority.fieldconsents.flarevent.vent.longterm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class VentLongTermTestUtil {
  public static List<VentLongTermYear> getVentLongTermYears(ApplicationVersion applicationVersion,
                                                            int startYear, int endYear) {
    List<VentLongTermYear> ventLongTermYears = new ArrayList<>();
    for(int year = endYear; year >= startYear; year--) {

      var ventLongTermYear = new VentLongTermYear();
      ventLongTermYear.setApplicationVersion(applicationVersion);
      ventLongTermYear.setYear(year);
      ventLongTermYear.setGas(new BigDecimal(year + 0.1));
      ventLongTermYears.add(ventLongTermYear);
    }

    return ventLongTermYears;
  }
}
