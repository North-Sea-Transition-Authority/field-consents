package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class ConsentLengthTestUtil {

  private static final int CONSENT_YEAR_LENGTH = 5;

  public static final int ANNUAL_CONSENT_YEAR = LocalDate.now().getYear() + 1;
  public static final int LONG_TERM_START_YEAR = LocalDate.now().getYear();
  public static final int LONG_TERM_END_YEAR = LONG_TERM_START_YEAR + 4;

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

  public static ConsentLengthForm getShortTermConsentLengthFormForDates(LocalDate startDate, LocalDate endDate) {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.SHORT_TERM);
    form.getShortTermStartDate().setDate(startDate);
    form.getShortTermEndDate().setDate(endDate);
    return form;
  }

  public static ConsentLengthForm getShortTermConsentLengthForm() {
    return getShortTermConsentLengthFormForDates(SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
  }

  public static ConsentLengthForm getAnnualConsentLengthForm() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.ANNUAL);
    form.getAnnualConsentYear().setInteger(ANNUAL_CONSENT_YEAR);
    return form;
  }

  public static ConsentLengthForm getLongTermConsentLengthFormForYears(int startYear, int endYear) {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.LONG_TERM);
    form.getLongTermStartYear().setInteger(startYear);
    form.getLongTermEndYear().setInteger(endYear);
    return form;
  }

  public static ConsentLengthForm getLongTermConsentLengthForm() {
    return getLongTermConsentLengthFormForYears(LONG_TERM_START_YEAR, LONG_TERM_END_YEAR);
  }

  public static ConsentLengthDetails getConsentLengthDetailsForShortTerm(ApplicationVersion applicationVersion,
                                                                         LocalDate startDate,
                                                                         LocalDate endDate) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setApplicationVersion(applicationVersion);
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(startDate);
    consentLengthDetails.setShortTermEndDate(endDate);
    return consentLengthDetails;
  }


  public static ConsentLengthDetails getConsentLengthDetailsForShortTerm(ApplicationVersion applicationVersion) {
    return getConsentLengthDetailsForShortTerm(applicationVersion, SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
  }

  public static ConsentLengthDetails getConsentLengthDetailsForAnnual(ApplicationVersion applicationVersion,
                                                                      Integer consentYear) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setApplicationVersion(applicationVersion);
    consentLengthDetails.setConsentLength(ConsentLengthType.ANNUAL);
    consentLengthDetails.setAnnualConsentYear(consentYear);
    return consentLengthDetails;
  }

  public static ConsentLengthDetails getConsentLengthDetailsForAnnual(ApplicationVersion applicationVersion) {
    return getConsentLengthDetailsForAnnual(applicationVersion, ANNUAL_CONSENT_YEAR);
  }

  public static ConsentLengthDetails getConsentLengthDetailsForLongTerm(ApplicationVersion applicationVersion,
                                                                        Integer startYear,
                                                                        Integer endYear) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setApplicationVersion(applicationVersion);
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(startYear);
    consentLengthDetails.setLongTermEndYear(endYear);
    return consentLengthDetails;
  }

  public static ConsentLengthDetails getConsentLengthDetailsForLongTerm(ApplicationVersion applicationVersion) {
    return getConsentLengthDetailsForLongTerm(applicationVersion, LONG_TERM_START_YEAR, LONG_TERM_END_YEAR);
  }
}
