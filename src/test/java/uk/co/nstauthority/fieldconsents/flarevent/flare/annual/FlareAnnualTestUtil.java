package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class FlareAnnualTestUtil {

  static ApplicationVersion flareAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

  static FlareAnnualMonthForm getFullFlareAnnualMonthForm() {
    FlareAnnualMonthForm flareAnnualMonthForm = new FlareAnnualMonthForm();
    flareAnnualMonthForm.setYear("2024");
    flareAnnualMonthForm.setMonth("April");
    flareAnnualMonthForm.setComments("Test form comments.");
    flareAnnualMonthForm.setCategoryA("1");
    flareAnnualMonthForm.setCategoryB("2");
    flareAnnualMonthForm.setCategoryC("3");
    return flareAnnualMonthForm;
  }

  static FlareAnnualMonth getFullFlareAnnualMonth() {
    FlareAnnualMonth flareAnnualMonth = new FlareAnnualMonth();
    flareAnnualMonth.setYear(2023);
    flareAnnualMonth.setMonth(Month.MARCH);
    flareAnnualMonth.setComments("Test comments.");
    flareAnnualMonth.setCategoryA(BigDecimal.ZERO);
    flareAnnualMonth.setCategoryB(BigDecimal.ONE);
    flareAnnualMonth.setCategoryC(BigDecimal.valueOf(999.999));
    return flareAnnualMonth;
  }

  static FlareAnnualForm getStubFlareAnnualFormForYear(int year) {
    FlareAnnualForm flareAnnualForm = new FlareAnnualForm();
    List<FlareAnnualMonthForm> flareAnnualMonthForms = new ArrayList<>();
    for (Month month: Month.values()) {
      flareAnnualMonthForms.add(FlareAnnualMonthForm.from(YearMonth.of(year, month)));
    }
    flareAnnualForm.setFlareAnnualMonthForms(flareAnnualMonthForms);
    return flareAnnualForm;
  }

  static FlareAnnualForm getFullFlareAnnualFormForYear(int year) {
    FlareAnnualForm flareAnnualForm = new FlareAnnualForm();
    List<FlareAnnualMonthForm> flareAnnualMonthForms = new ArrayList<>();

    FlareAnnualMonthForm flareAnnualMonthForm;
    for (Month month : Month.values()) {
      flareAnnualMonthForm = new FlareAnnualMonthForm();
      flareAnnualMonthForm.setYear(String.valueOf(year));
      flareAnnualMonthForm.setMonth(month);
      flareAnnualMonthForm.setComments("comment" + month.getValue());
      flareAnnualMonthForm.setCategoryA(String.valueOf(month.getValue()));
      flareAnnualMonthForm.setCategoryB(String.valueOf(month.getValue() * 10));
      flareAnnualMonthForm.setCategoryC(String.valueOf(month.getValue() * 100));

      flareAnnualMonthForms.add(flareAnnualMonthForm);
    }
    flareAnnualForm.setFlareAnnualMonthForms(flareAnnualMonthForms);
    return flareAnnualForm;
  }

  public static List<FlareAnnualMonth> getFlareAnnualMonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<FlareAnnualMonth> flareAnnualMonths = new ArrayList<>();

    FlareAnnualMonth flareAnnualMonth;
    for (Month month : Month.values()) {
      flareAnnualMonth = new FlareAnnualMonth();
      flareAnnualMonth.setApplicationVersion(applicationVersion);
      flareAnnualMonth.setYear(year);
      flareAnnualMonth.setMonth(month);
      flareAnnualMonth.setComments("comment" + month.getValue());
      flareAnnualMonth.setCategoryA(BigDecimal.valueOf(month.getValue()));
      flareAnnualMonth.setCategoryB(BigDecimal.valueOf(month.getValue() * 10));
      flareAnnualMonth.setCategoryC(BigDecimal.valueOf(month.getValue() * 100));

      flareAnnualMonths.add(flareAnnualMonth);
    }
    return flareAnnualMonths;
  }


}
