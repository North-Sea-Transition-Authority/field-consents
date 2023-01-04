package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

class VentReportTestUtil {

  static VentReportMonthForm getFullVentReportMonthForm() {
    VentReportMonthForm ventReportMonthForm = new VentReportMonthForm();
    ventReportMonthForm.setYear("2024");
    ventReportMonthForm.setMonth("April");
    ventReportMonthForm.setShutDownDays("10");
    ventReportMonthForm.setComments("Test form comments.");
    ventReportMonthForm.setCategoryA("1");
    ventReportMonthForm.setCategoryB("2");
    ventReportMonthForm.setCategoryC("3");
    return ventReportMonthForm;
  }

  static VentReportMonth getFullVentReportMonth() {
    VentReportMonth ventReportMonth = new VentReportMonth();
    ventReportMonth.setYear(2023);
    ventReportMonth.setMonth(Month.MARCH);
    ventReportMonth.setShutDownDays(15);
    ventReportMonth.setComments("Test comments.");
    ventReportMonth.setCategoryA(BigDecimal.ZERO);
    ventReportMonth.setCategoryB(BigDecimal.ONE);
    ventReportMonth.setCategoryC(BigDecimal.valueOf(999.999));
    return ventReportMonth;
  }

  static List<VentReportMonth> getVentReportMonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<VentReportMonth> ventReportMonths = new ArrayList<>();

    VentReportMonth ventReportMonth;
    for (Month month : Month.values()) {
      ventReportMonth = new VentReportMonth();
      ventReportMonth.setApplicationVersion(applicationVersion);
      ventReportMonth.setYear(year);
      ventReportMonth.setMonth(month);
      ventReportMonth.setShutDownDays(month.getValue());
      ventReportMonth.setComments("comment" + month.getValue());
      ventReportMonth.setCategoryA(BigDecimal.valueOf(month.getValue()));
      ventReportMonth.setCategoryB(BigDecimal.valueOf(month.getValue() * 10));
      ventReportMonth.setCategoryC(BigDecimal.valueOf(month.getValue() * 100));

      ventReportMonths.add(ventReportMonth);
    }
    return ventReportMonths;
  }

  static VentReportForm getStubVentReportForm() {
    VentReportForm ventReportForm = new VentReportForm();
    List<VentReportMonthForm> ventReportMonthForms = new ArrayList<>();
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2022, Month.NOVEMBER)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2022, Month.DECEMBER)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.JANUARY)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.FEBRUARY)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.MARCH)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.APRIL)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.MAY)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.JUNE)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.JULY)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.AUGUST)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.SEPTEMBER)));
    ventReportMonthForms.add(VentReportMonthForm.from(YearMonth.of(2023, Month.OCTOBER)));
    ventReportForm.setVentReportMonthForms(ventReportMonthForms);
    return ventReportForm;
  }

  static VentReportForm getFullVentReportFormForYear(int year) {
    VentReportForm ventReportForm = new VentReportForm();
    List<VentReportMonthForm> ventReportMonthForms = new ArrayList<>();

    VentReportMonthForm ventReportMonthForm;
    for (Month month : Month.values()) {
      ventReportMonthForm = new VentReportMonthForm();
      ventReportMonthForm.setYear(String.valueOf(year));
      ventReportMonthForm.setMonth(month);
      ventReportMonthForm.setShutDownDays(String.valueOf(month.getValue()));
      ventReportMonthForm.setComments("comment" + month.getValue());
      ventReportMonthForm.setCategoryA(String.valueOf(month.getValue()));
      ventReportMonthForm.setCategoryB(String.valueOf(month.getValue() * 10));
      ventReportMonthForm.setCategoryC(String.valueOf(month.getValue() * 100));

      ventReportMonthForms.add(ventReportMonthForm);
    }
    ventReportForm.setVentReportMonthForms(ventReportMonthForms);
    return ventReportForm;
  }

  static FlareVentReportPeriodForm getFullVentReportPeriodForm() {
    FlareVentReportPeriodForm form = new FlareVentReportPeriodForm();
    form.setReportEndMonth("APRIL");
    form.setReportEndYear("2023");
    return form;
  }

}
