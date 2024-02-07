package uk.co.nstauthority.fieldconsents.flarevent.flare.longterm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class FlareLongTermTestUtil {

  public static List<FlareLongTermYear> getFlareLongTermYears(ApplicationVersion applicationVersion,
                                                              int startYear, int endYear) {
    List<FlareLongTermYear> flareLongTermYears = new ArrayList<>();
    for(int year = endYear; year >= startYear; year--) {

      var flareLongTermYear = new FlareLongTermYear();
      flareLongTermYear.setApplicationVersion(applicationVersion);
      flareLongTermYear.setYear(year);
      flareLongTermYear.setGas(new BigDecimal(year + 0.1));
      flareLongTermYears.add(flareLongTermYear);
    }

    return flareLongTermYears;
  }
}
