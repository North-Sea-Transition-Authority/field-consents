package uk.co.nstauthority.fieldconsents.validation;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DEFAULT_ZONE_ID;
import static uk.co.nstauthority.fieldconsents.validation.FieldValidationErrorCodes.REQUIRED;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class ValidatorUtils {

  public static final int MAX_DECIMAL_PLACES = 6;
  public static final String EMPTY_STRING = "";
  public static final String PLURAL_STRING = "s";
  public static final String DATE_REQUIRED_ERROR_MESSAGE = "Pick a %s date";
  public static final String DATE_BEFORE_TODAY_ERROR_MESSAGE = "%s date must be on or after today";
  public static final String DATE_INVALID_ERROR_MESSAGE = "%s date must be a valid date in the format dd/mm/yyyy";
  public static final String TIME_REQUIRED_ERROR_MESSAGE = "Enter the %s time";
  public static final String TIME_INVALID_ERROR_MESSAGE = "%s time must be a real time";
  public static final String DATE_TIME_HOURS_AHEAD_ERROR_MESSAGE = "%s must be at least %s hour%s ahead of now";

  private ValidatorUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * invoke validator on a nested object while safely pushing and popping the nested object path.
   */
  public static void invokeNestedValidator(Errors errors,
                                           Validator validator,
                                           String targetPath,
                                           Object targetObject,
                                           Object... validationHints) {
    try {
      errors.pushNestedPath(targetPath);
      ValidationUtils.invokeValidator(validator, targetObject, errors, validationHints);
    } finally {
      errors.popNestedPath();
    }
  }

  public static void validateDateTimeIsInFutureByHours(String dateFieldName,
                                                       String hoursFieldName,
                                                       String minutesFieldName,
                                                       String displayName,
                                                       int mustBeHoursAhead,
                                                       String dateStr,
                                                       String hoursStr,
                                                       String minutesStr,
                                                       Clock clock,
                                                       Errors errors) {
    displayName = displayName.toLowerCase();

    // date validation
    validateDateCurrentOrInFuture(dateFieldName, displayName, dateStr, clock, errors);

    // time validation
    validateTime(hoursFieldName, minutesFieldName, displayName, hoursStr, minutesStr, errors);

    // check the date, hours and minutes together if they are valid individually
    if (errors.getFieldErrors(dateFieldName).isEmpty()
        && errors.getFieldErrors(hoursFieldName).isEmpty()
        && errors.getFieldErrors(minutesFieldName).isEmpty()) {

      var selectedDateTime = DateUtils.datePickerWithTimeStringToDateTime(dateStr, hoursStr, minutesStr);
      var futureDateTime = ZonedDateTime.ofInstant(clock.instant(), DEFAULT_ZONE_ID).plusHours(mustBeHoursAhead);

      if (selectedDateTime.isBefore(futureDateTime)) {
        errors.rejectValue(hoursFieldName, FieldValidationErrorCodes.BEFORE_SOME_DATE_TIME.errorCode(hoursFieldName),
            DATE_TIME_HOURS_AHEAD_ERROR_MESSAGE.formatted(
                StringUtils.capitalize(displayName), mustBeHoursAhead, addConditionalPlural(mustBeHoursAhead)));
        errors.rejectValue(minutesFieldName, FieldValidationErrorCodes.BEFORE_SOME_DATE_TIME.errorCode(minutesFieldName),
            EMPTY_STRING);
      }
    }
  }

  private static void validateDateCurrentOrInFuture(String dateFieldName,
                                                    String displayName,
                                                    String dateStr,
                                                    Clock clock,
                                                    Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, dateFieldName,
        REQUIRED.errorCode(dateFieldName), DATE_REQUIRED_ERROR_MESSAGE.formatted(displayName));

    // check the date if no errors already
    try {
      if (errors.getFieldErrors(dateFieldName).isEmpty()) {
        var date = DateUtils.datePickerStringToDate(dateStr);
        if (date.isBefore(LocalDate.ofInstant(clock.instant(), DEFAULT_ZONE_ID))) {
          errors.rejectValue(dateFieldName, FieldValidationErrorCodes.BEFORE_TODAY.errorCode(dateFieldName),
              DATE_BEFORE_TODAY_ERROR_MESSAGE.formatted(StringUtils.capitalize(displayName)));
        }
      }
    } catch (DateTimeParseException e) {
      errors.rejectValue(dateFieldName, FieldValidationErrorCodes.INVALID.errorCode(dateFieldName),
          DATE_INVALID_ERROR_MESSAGE.formatted(StringUtils.capitalize(displayName)));
    }
  }

  private static void validateTime(String hoursFieldName,
                                   String minutesFieldName,
                                   String displayName,
                                   String hoursStr,
                                   String minutesStr,
                                   Errors errors) {

    var timeRequiredErrorMessage = TIME_REQUIRED_ERROR_MESSAGE.formatted(displayName);
    var hoursEmptyOrWhiteSpace = (Objects.isNull(hoursStr) || !StringUtils.hasText(hoursStr));
    var minutesEmptyOrWhiteSpace = (Objects.isNull(minutesStr) || !StringUtils.hasText(minutesStr));

    if (hoursEmptyOrWhiteSpace && minutesEmptyOrWhiteSpace) { // both missing or whitespace
      errors.rejectValue(hoursFieldName, REQUIRED.errorCode(hoursFieldName), timeRequiredErrorMessage);
      errors.rejectValue(minutesFieldName, REQUIRED.errorCode(minutesFieldName), EMPTY_STRING);
      return;
    } else if (hoursEmptyOrWhiteSpace) { // only hours missing or whitespace
      errors.rejectValue(hoursFieldName, REQUIRED.errorCode(hoursFieldName), timeRequiredErrorMessage);
      return;
    } else if (minutesEmptyOrWhiteSpace) { // only minutes missing or whitespace
      errors.rejectValue(minutesFieldName, REQUIRED.errorCode(minutesFieldName), timeRequiredErrorMessage);
      return;
    }

    // if both hours and minutes are entered do further validation
    var timeInvalidErrorMessage = TIME_INVALID_ERROR_MESSAGE.formatted(StringUtils.capitalize(displayName));
    var hoursInvalid = !hoursWellFormed(hoursStr);
    var minutesInvalid = !minutesWellFormed(minutesStr);

    if (hoursInvalid && minutesInvalid) { // both invalid
      errors.rejectValue(hoursFieldName, FieldValidationErrorCodes.INVALID.errorCode(hoursFieldName),
          timeInvalidErrorMessage);
      errors.rejectValue(minutesFieldName, FieldValidationErrorCodes.INVALID.errorCode(minutesFieldName),
          EMPTY_STRING);
    } else if (hoursInvalid) { // only hours invalid
      errors.rejectValue(hoursFieldName, FieldValidationErrorCodes.INVALID.errorCode(hoursFieldName),
          timeInvalidErrorMessage);
    } else if (minutesInvalid) { // only minutes invalid
      errors.rejectValue(minutesFieldName, FieldValidationErrorCodes.INVALID.errorCode(minutesFieldName),
          timeInvalidErrorMessage);
    }
  }

  private static boolean hoursWellFormed(String hoursStr) {
    try {
      var hours = Integer.parseInt(hoursStr);
      return hours >= 0 && hours <= 23;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static boolean minutesWellFormed(String minutesStr) {
    try {
      var minutes = Integer.parseInt(minutesStr);
      return minutes >= 0 && minutes <= 59;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static String addConditionalPlural(int count) {
    return count > 1 ? PLURAL_STRING : EMPTY_STRING;
  }
}
