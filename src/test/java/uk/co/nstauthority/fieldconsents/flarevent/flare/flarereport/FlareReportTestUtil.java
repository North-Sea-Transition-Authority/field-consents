package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

public class FlareReportTestUtil {

  static ApplicationVersion flareAppVersion = ApplicationTestUtil.getApplicationVersionWithType(
      ApplicationType.FLARE);

  static FlareReportMonthForm getFullFlareReportMonthForm() {
    FlareReportMonthForm flareReportMonthForm = new FlareReportMonthForm();
    flareReportMonthForm.setYear("2024");
    flareReportMonthForm.setMonth("April");
    flareReportMonthForm.setShutDownDays("10");
    flareReportMonthForm.setComments("Test form comments.");
    flareReportMonthForm.setCategoryA("1");
    flareReportMonthForm.setCategoryB("2");
    flareReportMonthForm.setCategoryC("3");
    return flareReportMonthForm;
  }

  static FlareReportMonth getFullFlareReportMonth() {
    FlareReportMonth flareReportMonth = new FlareReportMonth();
    flareReportMonth.setYear(2023);
    flareReportMonth.setMonth(Month.MARCH);
    flareReportMonth.setShutDownDays(15);
    flareReportMonth.setComments("Test comments.");
    flareReportMonth.setCategoryA(BigDecimal.ZERO);
    flareReportMonth.setCategoryB(BigDecimal.ONE);
    flareReportMonth.setCategoryC(BigDecimal.valueOf(999.999));
    return flareReportMonth;
  }

  public static List<FlareReportMonth> getFlareReportMonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<FlareReportMonth> flareReportMonths = new ArrayList<>();

    FlareReportMonth flareReportMonth;
    for (Month month : Month.values()) {
      flareReportMonth = new FlareReportMonth();
      flareReportMonth.setApplicationVersion(applicationVersion);
      flareReportMonth.setYear(year);
      flareReportMonth.setMonth(month);
      flareReportMonth.setShutDownDays(month.getValue());
      flareReportMonth.setComments("comment" + month.getValue());
      flareReportMonth.setCategoryA(BigDecimal.valueOf(month.getValue()));
      flareReportMonth.setCategoryB(BigDecimal.valueOf(month.getValue() * 10));
      flareReportMonth.setCategoryC(BigDecimal.valueOf(month.getValue() * 100));

      flareReportMonths.add(flareReportMonth);
    }
    return flareReportMonths;
  }

  static FlareReportForm getStubFlareReportForm() {
    FlareReportForm flareReportForm = new FlareReportForm();
    List<FlareReportMonthForm> flareReportMonthForms = new ArrayList<>();
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2022, Month.NOVEMBER)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2022, Month.DECEMBER)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.JANUARY)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.FEBRUARY)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.MARCH)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.APRIL)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.MAY)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.JUNE)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.JULY)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.AUGUST)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.SEPTEMBER)));
    flareReportMonthForms.add(FlareReportMonthForm.from(YearMonth.of(2023, Month.OCTOBER)));
    flareReportForm.setFlareReportMonthForms(flareReportMonthForms);
    return flareReportForm;
  }

  static FlareReportForm getFullFlareReportFormForYear(int year) {
    FlareReportForm flareReportForm = new FlareReportForm();
    List<FlareReportMonthForm> flareReportMonthForms = new ArrayList<>();

    FlareReportMonthForm flareReportMonthForm;
    for (Month month : Month.values()) {
      flareReportMonthForm = new FlareReportMonthForm();
      flareReportMonthForm.setYear(String.valueOf(year));
      flareReportMonthForm.setMonth(month);
      flareReportMonthForm.setShutDownDays(String.valueOf(month.getValue()));
      flareReportMonthForm.setComments("comment" + month.getValue());
      flareReportMonthForm.setCategoryA(String.valueOf(month.getValue()));
      flareReportMonthForm.setCategoryB(String.valueOf(month.getValue() * 10));
      flareReportMonthForm.setCategoryC(String.valueOf(month.getValue() * 100));

      flareReportMonthForms.add(flareReportMonthForm);
    }
    flareReportForm.setFlareReportMonthForms(flareReportMonthForms);
    return flareReportForm;
  }

  public static FlareReportPeriod getFullFlareReportPeriod() {
    return new FlareReportPeriod(flareAppVersion, Month.APRIL, 2023);
  }

  static FlareVentReportPeriodForm getFullFlareReportPeriodForm() {
    FlareVentReportPeriodForm form = new FlareVentReportPeriodForm();
    form.setReportEndMonth("APRIL");
    form.setReportEndYear("2023");
    return form;
  }

}
