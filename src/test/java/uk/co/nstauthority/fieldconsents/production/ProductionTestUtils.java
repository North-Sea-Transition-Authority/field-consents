package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionForm;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonthForm;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionForm;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYearForm;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionForm;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonthForm;

/**
 * This util class is used to provide dummy data used for controller and service tests which refer to production data:
 * annual, short and long term.
 */
public class ProductionTestUtils {

  static public final Integer START_YEAR_LT = 2022;

  static public final Integer END_YEAR_LT = 2026;

  static public final String PRODUCTION_YEAR = "2022";

  public static final LocalDate START_DATE = LocalDate.of(2022, 12, 14);

  public static final LocalDate END_DATE = LocalDate.of(2023, 3, 1);

  public static final int START_MONTH_CONSENT_DAYS = YearMonth.of(START_DATE.getYear(),
      START_DATE.getMonth().ordinal() + 1).lengthOfMonth() - START_DATE.getDayOfMonth() + 1;

  public static AnnualProductionForm getEmptyAnnualProductionForm() {
    AnnualProductionForm annualProductionForm = new AnnualProductionForm();
    List<AnnualProductionMonthForm> annualProductionMonthForms = new LinkedList<>();

    for (Month month : Month.values()) {
      AnnualProductionMonthForm monthForm = getEmptyAnnualProductionMonthForm(month);
      annualProductionMonthForms.add(monthForm);
    }

    annualProductionForm.setYear(PRODUCTION_YEAR);
    annualProductionForm.setAnnualProductionMonthForms(annualProductionMonthForms);
    return annualProductionForm;
  }

  public static AnnualProductionForm getCompleteAnnualProductionForm() {
    AnnualProductionForm annualProductionForm = new AnnualProductionForm();
    List<AnnualProductionMonthForm> annualProductionMonthForms = new LinkedList<>();

    for (Month month : Month.values()) {
      AnnualProductionMonthForm monthForm = getCompleteAnnualProductionMonthForm(month);
      annualProductionMonthForms.add(monthForm);
    }

    annualProductionForm.setYear(PRODUCTION_YEAR);
    annualProductionForm.setAnnualProductionMonthForms(annualProductionMonthForms);
    return annualProductionForm;
  }

  @NotNull
  public static AnnualProductionMonthForm getCompleteAnnualProductionMonthForm(Month month) {
    AnnualProductionMonthForm monthForm = getEmptyAnnualProductionMonthForm(month);

    monthForm.getOilMinValue().setInputValue(String.valueOf(0.5));
    monthForm.getOilMaxValue().setInputValue(String.valueOf(2.3));
    monthForm.getGasMinValue().setInputValue(String.valueOf(1.72));
    monthForm.getGasMaxValue().setInputValue(String.valueOf(4.25));
    return monthForm;
  }

  @NotNull
  private static AnnualProductionMonthForm getEmptyAnnualProductionMonthForm(Month month) {
    ProductionRowForm productionRowForm = getProductionRowForm(ProductionUnit.SCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH);
    return new AnnualProductionMonthForm(
        month.name(),
        productionRowForm.getOilMinUnit(),
        productionRowForm.getOilMinValue(),
        productionRowForm.getOilMaxUnit(),
        productionRowForm.getOilMaxValue(),
        productionRowForm.getGasMinUnit(),
        productionRowForm.getGasMinValue(),
        productionRowForm.getGasMaxUnit(),
        productionRowForm.getGasMaxValue()
    );
  }

  @NotNull
  private static ProductionRowForm getProductionRowForm(ProductionUnit oilUnit, ProductionUnit gasUnit) {
    return new ProductionRowForm(
        oilUnit, new DecimalInput("oilMinValue", "Minimum oil"),
        oilUnit, new DecimalInput("oilMaxValue", "Maximum oil"),
        gasUnit, new DecimalInput("gasMinValue", "Minimum gas"),
        gasUnit, new DecimalInput("gasMaxValue", "Maximum gas")
    );
  }

  public static List<AnnualProductionMonth> getAnnualProductionMonthsData(ApplicationVersion applicationVersion) {
    List<AnnualProductionMonth> annualProductionMonths = new LinkedList<>();

    var allMonths = Month.values();

    for(int index = 0; index < Month.values().length; index++) {
      Month monthName = allMonths[index];
      AnnualProductionMonth annualProductionMonth = getAnnualProductionMonth(applicationVersion, index, monthName);
      annualProductionMonths.add(annualProductionMonth);
    }

    return annualProductionMonths;
  }

  @NotNull
  public static AnnualProductionMonth getAnnualProductionMonth(ApplicationVersion applicationVersion,
                                                               int annualProductionMonthId,
                                                               Month month) {
    AnnualProductionMonth annualProductionMonth = new AnnualProductionMonth();
    annualProductionMonth.setId(annualProductionMonthId);
    annualProductionMonth.setApplicationVersion(applicationVersion);
    annualProductionMonth.setYear(Integer.parseInt(PRODUCTION_YEAR));
    annualProductionMonth.setMonth(month);
    setProductionRowDetails(annualProductionMonth, ProductionUnit.SCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH);

    return annualProductionMonth;
  }

  @NotNull
  private static ShortTermProductionMonthForm getEmptyShortTermProductionMonthForm(LocalDate date, int consentDays) {
    ProductionRowForm productionRowForm = getProductionRowForm(ProductionUnit.SCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH);

    YearMonth yearMonth = YearMonth.of(
        date.getYear(),
        date.getMonth()
    );
    return new ShortTermProductionMonthForm(
        Character.toUpperCase(date.getMonth().name().charAt(0)) + date.getMonth().name().substring(1).toLowerCase(),
        String.valueOf(date.getYear()),
        consentDays,
        date,
        LocalDate.of(date.getYear(), date.getMonth(), yearMonth.lengthOfMonth()),
        productionRowForm.getOilMinUnit(),
        productionRowForm.getOilMinValue(),
        productionRowForm.getOilMaxUnit(),
        productionRowForm.getOilMaxValue(),
        productionRowForm.getGasMinUnit(),
        productionRowForm.getGasMinValue(),
        productionRowForm.getGasMaxUnit(),
        productionRowForm.getGasMaxValue()
    );
  }

  @NotNull
  public static ShortTermProductionMonthForm getCompleteShortTermProductionMonthForm(LocalDate date, int consentDays) {
    ShortTermProductionMonthForm monthForm = getEmptyShortTermProductionMonthForm(date, consentDays);

    monthForm.getOilMinValue().setInputValue(String.valueOf(0.5));
    monthForm.getOilMaxValue().setInputValue(String.valueOf(2.3));
    monthForm.getGasMinValue().setInputValue(String.valueOf(1.72));
    monthForm.getGasMaxValue().setInputValue(String.valueOf(4.25));
    return monthForm;
  }

  public static ShortTermProductionForm getEmptyShortTermProductionForm() {
    // Initialise basic form details
    ShortTermProductionForm shortTermProductionForm = new ShortTermProductionForm();

    List<ShortTermProductionMonthForm> shortTermProductionMonthForms = new LinkedList<>();

    // Initialise first month shown on the shortTermProductionForm
    ShortTermProductionMonthForm startMonthForm = new ShortTermProductionMonthForm();
    startMonthForm.setMonth(START_DATE.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    startMonthForm.setYear(String.valueOf(START_DATE.getYear()));
    startMonthForm.setConsentDays(START_MONTH_CONSENT_DAYS);
    startMonthForm.setStartDate(START_DATE);
    startMonthForm.setEndDate(END_DATE);
    startMonthForm.setOilMinUnit(ProductionUnit.SCM_PER_MONTH);
    startMonthForm.setOilMaxUnit(ProductionUnit.SCM_PER_MONTH);
    startMonthForm.setGasMinUnit(ProductionUnit.KSCM_PER_MONTH);
    startMonthForm.setGasMaxUnit(ProductionUnit.KSCM_PER_MONTH);
    shortTermProductionMonthForms.add(startMonthForm);

    // Initialise each month between start date and end date shown on the shortTermProductionForm
    for (LocalDate date = START_DATE.plusMonths(1);
         date.isBefore(END_DATE) && date.getMonth() != END_DATE.getMonth();
         date = date.plusMonths(1)) {
      int consentDays = YearMonth.of(date.getYear(), date.getMonth().ordinal() + 1).lengthOfMonth();
      ShortTermProductionMonthForm monthForm = getEmptyShortTermProductionMonthForm(date, consentDays);
      shortTermProductionMonthForms.add(monthForm);
    }

    // Initialise last month shown on the shortTermProductionForm
    ShortTermProductionMonthForm endMonthForm = new ShortTermProductionMonthForm();
    endMonthForm.setMonth(END_DATE.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    endMonthForm.setYear(String.valueOf(END_DATE.getYear()));
    endMonthForm.setConsentDays(END_DATE.getDayOfMonth());
    endMonthForm.setStartDate(START_DATE);
    endMonthForm.setEndDate(END_DATE);
    endMonthForm.setOilMinUnit(ProductionUnit.SCM_PER_MONTH);
    endMonthForm.setOilMaxUnit(ProductionUnit.SCM_PER_MONTH);
    endMonthForm.setGasMinUnit(ProductionUnit.KSCM_PER_MONTH);
    endMonthForm.setGasMaxUnit(ProductionUnit.KSCM_PER_MONTH);
    shortTermProductionMonthForms.add(endMonthForm);

    shortTermProductionForm.setShortTermProductionMonthForms(shortTermProductionMonthForms);
    return shortTermProductionForm;
  }

  public static List<ShortTermProductionMonth> getShortTermProductionMonthsData(ApplicationVersion applicationVersion) {
    List<ShortTermProductionMonth> shortTermProductionMonths = new LinkedList<>();
    int index = 0;

    // Initialise first month of the term
    ShortTermProductionMonth startProductionMonth = getShortTermProductionMonth(
        applicationVersion,
        index,
        START_DATE.getMonth(),
        START_DATE.getYear(),
        START_DATE,
        LocalDate.of(START_DATE.getYear(),
            START_DATE.getMonth(),
            START_DATE.getDayOfMonth() + START_MONTH_CONSENT_DAYS - 1));
    shortTermProductionMonths.add(startProductionMonth);

    // Initialise other months
    for(LocalDate date = START_DATE.plusMonths(1);
        date.isBefore(END_DATE) && date.getMonth() != END_DATE.getMonth();
        date = date.plusMonths(1)) {

      LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
      LocalDate endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());

      ShortTermProductionMonth shortTermProductionMonth = getShortTermProductionMonth(
          applicationVersion,
          index,
          date.getMonth(),
          date.getYear(),
          startDate,
          endDate);
      shortTermProductionMonths.add(shortTermProductionMonth);
    }
    // Initialise last month of the term
    ShortTermProductionMonth endProductionMonth = getShortTermProductionMonth(
        applicationVersion,
        index,
        END_DATE.getMonth(),
        END_DATE.getYear(),
        END_DATE,
        END_DATE);
    shortTermProductionMonths.add(endProductionMonth);

    return shortTermProductionMonths;
  }

  @NotNull
  public static ShortTermProductionMonth getShortTermProductionMonth(ApplicationVersion applicationVersion,
                                                                     int shortTermProductionMonthId,
                                                                     Month month,
                                                                     int year,
                                                                     LocalDate startDate,
                                                                     LocalDate endDate) {
    ShortTermProductionMonth shortTermProductionMonth = new ShortTermProductionMonth();
    shortTermProductionMonth.setId(shortTermProductionMonthId);
    shortTermProductionMonth.setApplicationVersion(applicationVersion);
    shortTermProductionMonth.setYear(year);
    shortTermProductionMonth.setMonth(month);
    shortTermProductionMonth.setStartDate(startDate);
    shortTermProductionMonth.setEndDate(endDate);
    setProductionRowDetails(shortTermProductionMonth, ProductionUnit.SCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH);

    return shortTermProductionMonth;
  }

  public static LongTermProductionForm getEmptyLongTermProductionForm(Integer startYear, Integer endYear) {
    var longTermProductionYearForms = new ArrayList<LongTermProductionYearForm>();

    for (Integer year = startYear; year <= endYear; year++) {
      longTermProductionYearForms.add(getEmptyLongTermProductionYearForm(year));
    }

    return new LongTermProductionForm(longTermProductionYearForms);
  }

  public static List<LongTermProductionYear> getLongTermProductionYearsData(ApplicationVersion applicationVersion) {

    List<LongTermProductionYear> longTermProductionYears = new ArrayList<>();
    for(Integer year = START_YEAR_LT; year <= END_YEAR_LT; year++) {
      longTermProductionYears.add(getLongTermProductionYear(applicationVersion, year - START_YEAR_LT + 1, year));
    }

    return longTermProductionYears;
  }

  @NotNull
  public static LongTermProductionYear getLongTermProductionYear(ApplicationVersion applicationVersion,
                                                                 Integer longTermProductionYearId,
                                                                 Integer year) {
    LongTermProductionYear longTermProductionYear = new LongTermProductionYear();
    longTermProductionYear.setId(longTermProductionYearId);
    longTermProductionYear.setApplicationVersion(applicationVersion);
    longTermProductionYear.setYear(year);
    setProductionRowDetails(longTermProductionYear, ProductionUnit.SCM_PER_DAY, ProductionUnit.KSCM_PER_DAY);

    return longTermProductionYear;
  }

  @NotNull
  public static LongTermProductionYearForm getCompleteLongTermProductionYearForm(Integer year) {
    LongTermProductionYearForm yearForm = getEmptyLongTermProductionYearForm(year);

    yearForm.getOilMinValue().setInputValue(String.valueOf(0.5));
    yearForm.getOilMaxValue().setInputValue(String.valueOf(2.3));
    yearForm.getGasMinValue().setInputValue(String.valueOf(1.72));
    yearForm.getGasMaxValue().setInputValue(String.valueOf(4.25));
    return yearForm;
  }

  @NotNull
  private static LongTermProductionYearForm getEmptyLongTermProductionYearForm(Integer year) {
    ProductionRowForm productionRowForm = getProductionRowForm(ProductionUnit.SCM_PER_DAY, ProductionUnit.KSCM_PER_DAY);
    return new LongTermProductionYearForm(
        year.toString(),
        productionRowForm.getOilMinUnit(),
        productionRowForm.getOilMinValue(),
        productionRowForm.getOilMaxUnit(),
        productionRowForm.getOilMaxValue(),
        productionRowForm.getGasMinUnit(),
        productionRowForm.getGasMinValue(),
        productionRowForm.getGasMaxUnit(),
        productionRowForm.getGasMaxValue()
    );
  }

  private static void setProductionRowDetails(ProductionRow productionRow, ProductionUnit oilUnit, ProductionUnit gasUnit) {
    productionRow.setOilMinUnit(oilUnit);
    productionRow.setOilMinValue(new BigDecimal("0.5"));
    productionRow.setOilMaxUnit(oilUnit);
    productionRow.setOilMaxValue(new BigDecimal("2.3"));
    productionRow.setGasMinUnit(gasUnit);
    productionRow.setGasMinValue(new BigDecimal("1.72"));
    productionRow.setGasMaxUnit(gasUnit);
    productionRow.setGasMaxValue(new BigDecimal("4.25"));
  }
}
