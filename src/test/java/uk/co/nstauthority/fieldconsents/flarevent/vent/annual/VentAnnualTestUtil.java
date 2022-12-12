package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class VentAnnualTestUtil {

  static ApplicationVersion ventAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);

  static VentAnnualMonthForm getFullVentAnnualMonthForm() {
    VentAnnualMonthForm ventAnnualMonthForm = new VentAnnualMonthForm();
    ventAnnualMonthForm.setYear("2024");
    ventAnnualMonthForm.setMonth("April");
    ventAnnualMonthForm.setComments("Test form comments.");
    ventAnnualMonthForm.setCategoryA("1");
    ventAnnualMonthForm.setCategoryB("2");
    ventAnnualMonthForm.setCategoryC("3");
    return ventAnnualMonthForm;
  }

  static VentAnnualMonth getFullVentAnnualMonth() {
    VentAnnualMonth ventAnnualMonth = new VentAnnualMonth();
    ventAnnualMonth.setYear(2023);
    ventAnnualMonth.setMonth(Month.MARCH);
    ventAnnualMonth.setComments("Test comments.");
    ventAnnualMonth.setCategoryA(BigDecimal.ZERO);
    ventAnnualMonth.setCategoryB(BigDecimal.ONE);
    ventAnnualMonth.setCategoryC(BigDecimal.valueOf(999.999));
    return ventAnnualMonth;
  }

  static VentAnnualForm getStubVentAnnualFormForYear(int year) {
    VentAnnualForm ventAnnualForm = new VentAnnualForm();
    List<VentAnnualMonthForm> ventAnnualMonthForms = new ArrayList<>();
    for (Month month: Month.values()) {
      ventAnnualMonthForms.add(VentAnnualMonthForm.from(YearMonth.of(year, month)));
    }
    ventAnnualForm.setVentAnnualMonthForms(ventAnnualMonthForms);
    return ventAnnualForm;
  }

  static VentAnnualForm getFullVentAnnualFormForYear(int year) {
    VentAnnualForm ventAnnualForm = new VentAnnualForm();
    List<VentAnnualMonthForm> ventAnnualMonthForms = new ArrayList<>();

    VentAnnualMonthForm ventAnnualMonthForm;
    for (Month month : Month.values()) {
      ventAnnualMonthForm = new VentAnnualMonthForm();
      ventAnnualMonthForm.setYear(String.valueOf(year));
      ventAnnualMonthForm.setMonth(month);
      ventAnnualMonthForm.setComments("comment" + month.getValue());
      ventAnnualMonthForm.setCategoryA(String.valueOf(month.getValue()));
      ventAnnualMonthForm.setCategoryB(String.valueOf(month.getValue() * 10));
      ventAnnualMonthForm.setCategoryC(String.valueOf(month.getValue() * 100));

      ventAnnualMonthForms.add(ventAnnualMonthForm);
    }
    ventAnnualForm.setVentAnnualMonthForms(ventAnnualMonthForms);
    return ventAnnualForm;
  }

  public static List<VentAnnualMonth> getVentAnnualMonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<VentAnnualMonth> ventAnnualMonths = new ArrayList<>();

    VentAnnualMonth ventAnnualMonth;
    for (Month month : Month.values()) {
      ventAnnualMonth = new VentAnnualMonth();
      ventAnnualMonth.setApplicationVersion(applicationVersion);
      ventAnnualMonth.setYear(year);
      ventAnnualMonth.setMonth(month);
      ventAnnualMonth.setComments("comment" + month.getValue());
      ventAnnualMonth.setCategoryA(BigDecimal.valueOf(month.getValue()));
      ventAnnualMonth.setCategoryB(BigDecimal.valueOf(month.getValue() * 10));
      ventAnnualMonth.setCategoryC(BigDecimal.valueOf(month.getValue() * 100));

      ventAnnualMonths.add(ventAnnualMonth);
    }
    return ventAnnualMonths;
  }

}
