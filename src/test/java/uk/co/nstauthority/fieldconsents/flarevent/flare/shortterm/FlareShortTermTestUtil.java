package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

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

class FlareShortTermTestUtil {

  static ApplicationVersion flareAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

  static FlareShortTermMonthForm getFullFlareShortTermMonthForm() {
    FlareShortTermMonthForm flareShortTermMonthForm = new FlareShortTermMonthForm();
    flareShortTermMonthForm.setYear("2024");
    flareShortTermMonthForm.setMonth("April");
    flareShortTermMonthForm.setComments("Test form comments.");
    flareShortTermMonthForm.setCategoryA("1");
    flareShortTermMonthForm.setCategoryB("2");
    flareShortTermMonthForm.setCategoryC("3");
    flareShortTermMonthForm.setStartDate(LocalDate.of(2024, Month.APRIL, 11));
    flareShortTermMonthForm.setEndDate(LocalDate.of(2024, Month.APRIL, 20));
    flareShortTermMonthForm.setConsentDays(10);
    return flareShortTermMonthForm;
  }

  static FlareShortTermMonth getFullFlareShortTermMonth() {
    FlareShortTermMonth flareShortTermMonth = new FlareShortTermMonth();
    flareShortTermMonth.setYear(2023);
    flareShortTermMonth.setMonth(Month.MARCH);
    flareShortTermMonth.setComments("Test comments.");
    flareShortTermMonth.setCategoryA(BigDecimal.ZERO);
    flareShortTermMonth.setCategoryB(BigDecimal.ONE);
    flareShortTermMonth.setCategoryC(BigDecimal.valueOf(999.999));
    flareShortTermMonth.setStartDate(LocalDate.of(2023, Month.MARCH, 1));
    flareShortTermMonth.setEndDate(LocalDate.of(2023, Month.MARCH, 15));
    return flareShortTermMonth;
  }

  static FlareShortTermForm getStubFlareShortTermFormForPeriod(LocalDate startDate, LocalDate endDate) {
    FlareShortTermForm flareShortTermForm = new FlareShortTermForm();
    List<FlareShortTermMonthForm> flareShortTermMonthForms = new ArrayList<>();
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      flareShortTermMonthForms.add(FlareShortTermMonthForm.from(shortTermMonth.getLeft(), shortTermMonth.getRight()));
    }
    flareShortTermForm.setFlareShortTermMonthForms(flareShortTermMonthForms);
    return flareShortTermForm;
  }

  static FlareShortTermForm getFullFlareShortTermFormForPeriod(LocalDate startDate, LocalDate endDate) {
    FlareShortTermForm flareShortTermForm = new FlareShortTermForm();
    List<FlareShortTermMonthForm> flareShortTermMonthForms = new ArrayList<>();

    FlareShortTermMonthForm flareShortTermMonthForm;
    int rowNumber = 1;
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      flareShortTermMonthForm = FlareShortTermMonthForm.from(shortTermMonth.getLeft(), shortTermMonth.getRight());
      flareShortTermMonthForm.setCategoryA(String.valueOf(rowNumber));
      flareShortTermMonthForm.setCategoryB(String.valueOf(rowNumber * 10));
      flareShortTermMonthForm.setCategoryC(String.valueOf(rowNumber * 100));
      flareShortTermMonthForm.setComments("comment" + rowNumber);

      flareShortTermMonthForms.add(flareShortTermMonthForm);
      rowNumber++;
    }
    flareShortTermForm.setFlareShortTermMonthForms(flareShortTermMonthForms);
    return flareShortTermForm;
  }

  static List<FlareShortTermMonth> getFlareShortTermMonthsForPeriod(ApplicationVersion applicationVersion,
                                                                    LocalDate startDate, LocalDate endDate) {
    List<FlareShortTermMonth> flareShortTermMonths = new ArrayList<>();

    FlareShortTermMonth flareShortTermMonth;
    int rowNumber = 1;
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      flareShortTermMonth = new FlareShortTermMonth();
      flareShortTermMonth.setApplicationVersion(applicationVersion);
      flareShortTermMonth.setStartDate(shortTermMonth.getLeft());
      flareShortTermMonth.setEndDate(shortTermMonth.getRight());
      flareShortTermMonth.setYear(flareShortTermMonth.getStartDate().getYear());
      flareShortTermMonth.setMonth(flareShortTermMonth.getStartDate().getMonth());
      flareShortTermMonth.setComments("comment" + rowNumber);
      flareShortTermMonth.setCategoryA(BigDecimal.valueOf(rowNumber));
      flareShortTermMonth.setCategoryB(BigDecimal.valueOf(rowNumber * 10));
      flareShortTermMonth.setCategoryC(BigDecimal.valueOf(rowNumber * 100));

      flareShortTermMonths.add(flareShortTermMonth);
      rowNumber++;
    }
    return flareShortTermMonths;
  }

}
