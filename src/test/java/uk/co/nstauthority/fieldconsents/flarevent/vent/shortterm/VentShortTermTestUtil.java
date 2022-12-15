package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ShortTermUtil;

public class VentShortTermTestUtil {

  static ApplicationVersion ventAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);

  static VentShortTermMonthForm getFullVentShortTermMonthForm() {
    VentShortTermMonthForm ventShortTermMonthForm = new VentShortTermMonthForm();
    ventShortTermMonthForm.setYear("2024");
    ventShortTermMonthForm.setMonth("April");
    ventShortTermMonthForm.setComments("Test form comments.");
    ventShortTermMonthForm.setCategoryA("1");
    ventShortTermMonthForm.setCategoryB("2");
    ventShortTermMonthForm.setCategoryC("3");
    ventShortTermMonthForm.setStartDate(LocalDate.of(2024, Month.APRIL, 11));
    ventShortTermMonthForm.setEndDate(LocalDate.of(2024, Month.APRIL, 20));
    ventShortTermMonthForm.setConsentDays(10);
    return ventShortTermMonthForm;
  }

  static VentShortTermMonth getFullVentShortTermMonth() {
    VentShortTermMonth ventShortTermMonth = new VentShortTermMonth();
    ventShortTermMonth.setYear(2023);
    ventShortTermMonth.setMonth(Month.MARCH);
    ventShortTermMonth.setComments("Test comments.");
    ventShortTermMonth.setCategoryA(BigDecimal.ZERO);
    ventShortTermMonth.setCategoryB(BigDecimal.ONE);
    ventShortTermMonth.setCategoryC(BigDecimal.valueOf(999.999));
    ventShortTermMonth.setStartDate(LocalDate.of(2023, Month.MARCH, 1));
    ventShortTermMonth.setEndDate(LocalDate.of(2023, Month.MARCH, 15));
    return ventShortTermMonth;
  }

  static VentShortTermForm getStubVentShortTermFormForPeriod(LocalDate startDate, LocalDate endDate) {
    VentShortTermForm ventShortTermForm = new VentShortTermForm();
    List<VentShortTermMonthForm> ventShortTermMonthForms = new ArrayList<>();
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      ventShortTermMonthForms.add(VentShortTermMonthForm.from(shortTermMonth.getLeft(), shortTermMonth.getRight()));
    }
    ventShortTermForm.setVentShortTermMonthForms(ventShortTermMonthForms);
    return ventShortTermForm;
  }

  static VentShortTermForm getFullVentShortTermFormForPeriod(LocalDate startDate, LocalDate endDate) {
    VentShortTermForm ventShortTermForm = new VentShortTermForm();
    List<VentShortTermMonthForm> ventShortTermMonthForms = new ArrayList<>();

    VentShortTermMonthForm ventShortTermMonthForm;
    int rowNumber = 1;
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      ventShortTermMonthForm = VentShortTermMonthForm.from(shortTermMonth.getLeft(), shortTermMonth.getRight());
      ventShortTermMonthForm.setCategoryA(String.valueOf(rowNumber));
      ventShortTermMonthForm.setCategoryB(String.valueOf(rowNumber * 10));
      ventShortTermMonthForm.setCategoryC(String.valueOf(rowNumber * 100));
      ventShortTermMonthForm.setComments("comment" + rowNumber);

      ventShortTermMonthForms.add(ventShortTermMonthForm);
      rowNumber++;
    }
    ventShortTermForm.setVentShortTermMonthForms(ventShortTermMonthForms);
    return ventShortTermForm;
  }

  public static List<VentShortTermMonth> getVentShortTermMonthsForPeriod(ApplicationVersion applicationVersion,
                                                                          LocalDate startDate, LocalDate endDate) {
    List<VentShortTermMonth> ventShortTermMonths = new ArrayList<>();

    VentShortTermMonth ventShortTermMonth;
    int rowNumber = 1;
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      ventShortTermMonth = new VentShortTermMonth();
      ventShortTermMonth.setApplicationVersion(applicationVersion);
      ventShortTermMonth.setStartDate(shortTermMonth.getLeft());
      ventShortTermMonth.setEndDate(shortTermMonth.getRight());
      ventShortTermMonth.setYear(ventShortTermMonth.getStartDate().getYear());
      ventShortTermMonth.setMonth(ventShortTermMonth.getStartDate().getMonth());
      ventShortTermMonth.setComments("comment" + rowNumber);
      ventShortTermMonth.setCategoryA(BigDecimal.valueOf(rowNumber));
      ventShortTermMonth.setCategoryB(BigDecimal.valueOf(rowNumber * 10L));
      ventShortTermMonth.setCategoryC(BigDecimal.valueOf(rowNumber * 100L));

      ventShortTermMonths.add(ventShortTermMonth);
      rowNumber++;
    }
    return ventShortTermMonths;
  }

}
