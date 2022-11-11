package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class ConsentLengthTestUtil {

  private static final int CONSENT_YEAR_LENGTH = 5;

  public static final int ANNUAL_CONSENT_YEAR = 2023;
  public static final int LONG_TERM_START_YEAR = 2023;
  public static final int LONG_TERM_END_YEAR = 2027;

  public static final LocalDate SHORT_TERM_START_DATE = LocalDate.of(2022, Month.OCTOBER, 31);

  public static final LocalDate SHORT_TERM_END_DATE = LocalDate.of(2023, Month.APRIL, 12);

  static Map<String, String> getConsentLengthTypeMap() {
    return Arrays.stream(ConsentLengthType.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ConsentLengthType::getDisplayName));
  }

  static Map<String, String> getAnnualConsentYearsMap() {
    return Map.of(
        String.valueOf(LocalDate.now().getYear() + 1),
        String.valueOf(LocalDate.now().getYear() + 2)
    );
  }

  static Map<String, String> getLongTermConsentYearsMap() {
    Map<String, String> consentYearList = new HashMap<>();
    for (int index = 0; index < CONSENT_YEAR_LENGTH; index++) {
      String year = String.valueOf(LocalDate.now().getYear() + index);
      consentYearList.put(year, year);
    }
    return consentYearList;
  }

  public static ConsentLengthForm getShortTermConsentLengthForm() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.SHORT_TERM);

    IntegerInput shortTermStartDay = new IntegerInput("shortTermStartDay", "Day");
    shortTermStartDay.setInputValue(String.valueOf(SHORT_TERM_START_DATE.getDayOfMonth()));
    form.setShortTermStartDay(shortTermStartDay);
    IntegerInput shortTermStartMonth = new IntegerInput("shortTermStartMonth", "Month");
    shortTermStartMonth.setInputValue(String.valueOf(SHORT_TERM_START_DATE.getMonthValue()));
    form.setShortTermStartMonth(shortTermStartMonth);
    IntegerInput shortTermStartYear = new IntegerInput("shortTermStartYear", "Year");
    shortTermStartYear.setInputValue(String.valueOf(SHORT_TERM_START_DATE.getYear()));
    form.setShortTermStartYear(shortTermStartYear);
    ThreeFieldDateInput shortTermStartDateInput = new ThreeFieldDateInput("shortTermStartDate", "Start date",
        shortTermStartDay, shortTermStartMonth, shortTermStartYear);
    form.setShortTermStartDate(shortTermStartDateInput);

    IntegerInput shortTermEndDay = new IntegerInput("shortTermEndDay", "Day");
    shortTermEndDay.setInputValue(String.valueOf(SHORT_TERM_END_DATE.getDayOfMonth()));
    form.setShortTermEndDay(shortTermEndDay);
    IntegerInput shortTermEndMonth = new IntegerInput("shortTermEndMonth", "Month");
    shortTermEndMonth.setInputValue(String.valueOf(SHORT_TERM_END_DATE.getMonthValue()));
    form.setShortTermEndMonth(shortTermEndMonth);
    IntegerInput shortTermEndYear = new IntegerInput("shortTermEndYear", "Year");
    shortTermEndYear.setInputValue(String.valueOf(SHORT_TERM_END_DATE.getYear()));
    form.setShortTermEndYear(shortTermEndYear);
    ThreeFieldDateInput shortTermEndDateInput = new ThreeFieldDateInput("shortTermEndDate", "End date",
        shortTermEndDay, shortTermEndMonth, shortTermEndYear);
    form.setShortTermEndDate(shortTermEndDateInput);
    return form;
  }

  public static ConsentLengthForm getAnnualConsentLengthForm() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.ANNUAL);
    IntegerInput annualConsentYear = new IntegerInput("annualConsentYear", "Year");
    annualConsentYear.setInputValue("2023");
    form.setAnnualConsentYear(annualConsentYear);
    return form;
  }

  public static ConsentLengthForm getLongTermConsentLengthForm() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.LONG_TERM);
    IntegerInput longTermStartYear = new IntegerInput("longTermStartYear", "Year");
    longTermStartYear.setInputValue(String.valueOf(LONG_TERM_START_YEAR));
    form.setLongTermStartYear(longTermStartYear);
    IntegerInput longTermEndYear = new IntegerInput("longTermEndYear", "Year");
    longTermEndYear.setInputValue(String.valueOf(LONG_TERM_END_YEAR));
    form.setLongTermEndYear(longTermEndYear);
    return form;
  }

  public static ConsentLengthDetails getConsentLengthDetailsForShortTerm(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setApplicationVersion(applicationVersion);
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(SHORT_TERM_START_DATE);
    consentLengthDetails.setShortTermEndDate(SHORT_TERM_END_DATE);
    return consentLengthDetails;
  }

  public static ConsentLengthDetails getConsentLengthDetailsForAnnual(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setApplicationVersion(applicationVersion);
    consentLengthDetails.setConsentLength(ConsentLengthType.ANNUAL);
    consentLengthDetails.setAnnualConsentYear(ANNUAL_CONSENT_YEAR);
    return consentLengthDetails;
  }

  public static ConsentLengthDetails getConsentLengthDetailsForLongTerm(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setApplicationVersion(applicationVersion);
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(LONG_TERM_START_YEAR);
    consentLengthDetails.setLongTermEndYear(LONG_TERM_END_YEAR);
    return consentLengthDetails;
  }
}
